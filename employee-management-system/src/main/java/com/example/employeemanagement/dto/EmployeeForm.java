package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeForm {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 500)
    private String address;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotNull(message = "Join date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate joinDate;

    @Size(max = 100)
    private String designation;

    private Long departmentId;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.00", message = "Salary cannot be negative")
    private BigDecimal basicSalary;

    // --- account (username only editable on create) ---
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Letters, digits, dot, dash and underscore only")
    private String username;

    @NotNull(message = "Role is required")
    private Role role = Role.ROLE_EMPLOYEE;

    /** Required on create, optional on edit (blank = keep current password). */
    private String password;
}
