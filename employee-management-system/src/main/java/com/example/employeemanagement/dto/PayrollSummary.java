package com.example.employeemanagement.dto;

import java.math.BigDecimal;

public record PayrollSummary(String label, long count, BigDecimal gross, BigDecimal deductions, BigDecimal net) {
}
