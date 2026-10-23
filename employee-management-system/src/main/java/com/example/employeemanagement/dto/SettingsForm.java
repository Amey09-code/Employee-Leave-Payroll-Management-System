package com.example.employeemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SettingsForm {

    @NotBlank
    private String companyName;

    @NotNull @Min(0) @Max(365)
    private Integer annualLeaveAllowance;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal hraPercent;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal allowancePercent;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal pfPercent;
}
