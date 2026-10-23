package com.example.employeemanagement.entity;

public enum LeaveType {
    ANNUAL("Annual Leave"), SICK("Sick Leave"), CASUAL("Casual Leave"), UNPAID("Unpaid Leave");

    private final String label;
    LeaveType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
