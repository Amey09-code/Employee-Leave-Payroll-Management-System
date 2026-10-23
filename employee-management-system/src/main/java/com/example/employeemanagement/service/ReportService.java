package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.AttendanceReportRow;
import com.example.employeemanagement.dto.ReportData;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.LeaveStatus;
import com.example.employeemanagement.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final AttendanceRepository attendanceRepository;

    public ReportData build(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        int workingDays = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        final int wd = workingDays;

        Map<LeaveStatus, Long> leaves = new EnumMap<>(LeaveStatus.class);
        for (LeaveStatus s : LeaveStatus.values()) {
            leaves.put(s, leaveService.countByStatus(s));
        }
        List<AttendanceReportRow> attendance = employeeService.findActive().stream()
                .map((Employee e) -> new AttendanceReportRow(e.getFullName(),
                        e.getDepartment() == null ? "-" : e.getDepartment().getName(),
                        attendanceRepository.countWorkedDays(e, from, to), wd))
                .toList();

        return new ReportData(month, year, departmentService.findAllWithCounts(), leaves,
                payrollService.trend(6), attendance);
    }
}
