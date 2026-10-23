package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Department;

public record DepartmentRow(Department department, long employeeCount) {
}
