package com.example.employeemanagement.dto;

import java.math.BigDecimal;

public record PayrollSettings(BigDecimal hraPercent, BigDecimal allowancePercent, BigDecimal pfPercent) {
}
