package com.example.employeemanagement.entity;

public enum AttendanceStatus {
    PRESENT("bg-success"), HALF_DAY("bg-warning text-dark"), ABSENT("bg-danger");

    private final String badgeClass;
    AttendanceStatus(String badgeClass) { this.badgeClass = badgeClass; }
    public String getBadgeClass() { return badgeClass; }
}
