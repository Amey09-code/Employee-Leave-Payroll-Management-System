package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.AttendanceStatus;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.repository.AttendanceRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private static final Duration HALF_DAY_THRESHOLD = Duration.ofHours(4);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public Attendance checkIn(String username) {
        Employee e = activeEmployee(username);
        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByEmployeeAndWorkDate(e, today).isPresent()) {
            throw new BusinessException("You have already checked in today.");
        }
        Attendance a = new Attendance();
        a.setEmployee(e);
        a.setWorkDate(today);
        a.setCheckIn(LocalTime.now().withNano(0));
        a.setStatus(AttendanceStatus.PRESENT);
        return attendanceRepository.save(a);
    }

    public Attendance checkOut(String username) {
        Employee e = activeEmployee(username);
        Attendance a = attendanceRepository.findByEmployeeAndWorkDate(e, LocalDate.now())
                .orElseThrow(() -> new BusinessException("You have not checked in today."));
        if (a.getCheckOut() != null) {
            throw new BusinessException("You have already checked out today.");
        }
        a.setCheckOut(LocalTime.now().withNano(0));
        if (Duration.between(a.getCheckIn(), a.getCheckOut()).compareTo(HALF_DAY_THRESHOLD) < 0) {
            a.setStatus(AttendanceStatus.HALF_DAY);
        }
        return attendanceRepository.save(a);
    }

    @Transactional(readOnly = true)
    public Optional<Attendance> today(String username) {
        Employee e = employeeService.getByUsername(username);
        return attendanceRepository.findByEmployeeAndWorkDate(e, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Page<Attendance> history(String username, int page, int size) {
        Employee e = employeeService.getByUsername(username);
        return attendanceRepository.findByEmployeeOrderByWorkDateDesc(e, PageRequest.of(Math.max(page, 0), size));
    }

    @Transactional(readOnly = true)
    public Page<Attendance> byDate(LocalDate date, int page, int size) {
        return attendanceRepository.findByWorkDate(date, PageRequest.of(Math.max(page, 0), size));
    }

    @Transactional(readOnly = true)
    public long presentToday() {
        return attendanceRepository.countByWorkDateAndCheckInIsNotNull(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long absentToday() {
        return Math.max(0, employeeRepository.countByActiveTrue() - presentToday());
    }

    @Transactional(readOnly = true)
    public String exportCsv(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessException("'To' date must not be before 'From' date.");
        }
        List<Attendance> rows = attendanceRepository.findInRange(from, to);
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
        StringBuilder sb = new StringBuilder("Date,Employee Code,Employee,Department,Check In,Check Out,Worked,Status\n");
        for (Attendance a : rows) {
            sb.append(a.getWorkDate()).append(',')
              .append(csv(a.getEmployee().getEmployeeCode())).append(',')
              .append(csv(a.getEmployee().getFullName())).append(',')
              .append(csv(a.getEmployee().getDepartment() == null ? "" : a.getEmployee().getDepartment().getName())).append(',')
              .append(a.getCheckIn() == null ? "" : a.getCheckIn().format(tf)).append(',')
              .append(a.getCheckOut() == null ? "" : a.getCheckOut().format(tf)).append(',')
              .append(csv(a.getWorkedHours())).append(',')
              .append(a.getStatus()).append('\n');
        }
        return sb.toString();
    }

    /** Escapes a CSV cell and neutralises spreadsheet formula injection (cells starting with = + - @). */
    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private Employee activeEmployee(String username) {
        Employee e = employeeService.getByUsername(username);
        if (!e.isActive()) {
            throw new BusinessException("Your account is inactive.");
        }
        return e;
    }
}
