package com.example.employeemanagement.dto;

import java.math.BigDecimal;

public record HrDashboardStats(
        long totalEmployees,
        long presentToday,
        long absentToday,
        long pendingLeaves,
        int payrollMonth,
        int payrollYear,
        long payslipsGenerated,
        BigDecimal payrollGross,
        BigDecimal payrollNet) {
}
