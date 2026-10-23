package com.example.employeemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollForm {

    /** Empty = all active employees. */
    private Long employeeId;

    @NotNull @Min(1) @Max(12)
    private Integer month;

    @NotNull @Min(2000) @Max(2100)
    private Integer year;

    @DecimalMin("0.00")
    private BigDecimal bonus = BigDecimal.ZERO;

    @DecimalMin("0.00")
    private BigDecimal otherDeductions = BigDecimal.ZERO;
}
