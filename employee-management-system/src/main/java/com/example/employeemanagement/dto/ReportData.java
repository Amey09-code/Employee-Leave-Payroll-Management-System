package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.LeaveStatus;

import java.util.List;
import java.util.Map;

public record ReportData(
        int month,
        int year,
        List<DepartmentRow> headcount,
        Map<LeaveStatus, Long> leavesByStatus,
        List<PayrollSummary> payrollTrend,
        List<AttendanceReportRow> attendance) {
}
