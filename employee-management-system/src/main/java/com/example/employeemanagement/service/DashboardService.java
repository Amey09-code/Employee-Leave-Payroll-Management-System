package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeOverview;
import com.example.employeemanagement.dto.HrDashboardStats;
import com.example.employeemanagement.dto.PayrollSummary;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final HolidayService holidayService;

    public HrDashboardStats hrStats() {
        LocalDate now = LocalDate.now();
        PayrollSummary p = payrollService.summary(now.getMonthValue(), now.getYear());
        return new HrDashboardStats(
                employeeRepository.countByActiveTrue(),
                attendanceService.presentToday(),
                attendanceService.absentToday(),
                leaveService.pendingCount(),
                now.getMonthValue(), now.getYear(),
                p.count(), p.gross(), p.net());
    }

    public EmployeeOverview employeeOverview(String username) {
        Employee e = employeeService.getByUsername(username);
        return new EmployeeOverview(
                e,
                attendanceService.today(username).orElse(null),
                leaveService.balance(e),
                payrollService.latestFor(username),
                holidayService.upcoming().stream().limit(5).toList());
    }
}
