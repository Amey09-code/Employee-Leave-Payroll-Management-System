package com.example.employeemanagement.entity;

public enum SalaryStatus {
    GENERATED("bg-warning text-dark"), PAID("bg-success");

    private final String badgeClass;
    SalaryStatus(String badgeClass) { this.badgeClass = badgeClass; }
    public String getBadgeClass() { return badgeClass; }
}
