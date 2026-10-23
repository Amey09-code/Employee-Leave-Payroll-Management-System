package com.example.employeemanagement.entity;

public enum LeaveStatus {
    PENDING("bg-warning text-dark"), APPROVED("bg-success"), REJECTED("bg-danger");

    private final String badgeClass;
    LeaveStatus(String badgeClass) { this.badgeClass = badgeClass; }
    public String getBadgeClass() { return badgeClass; }
}
