package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.Holiday;
import com.example.employeemanagement.entity.Salary;

import java.util.List;

/** Everything the employee dashboard needs. attendanceToday and latestSalary may be null. */
public record EmployeeOverview(
        Employee employee,
        Attendance attendanceToday,
        LeaveBalance leaveBalance,
        Salary latestSalary,
        List<Holiday> upcomingHolidays) {
}
