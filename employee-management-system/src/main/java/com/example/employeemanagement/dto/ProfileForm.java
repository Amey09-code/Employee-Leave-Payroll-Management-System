package com.example.employeemanagement.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Fields an employee may edit about themself. */
@Getter
@Setter
public class ProfileForm {

    @Size(max = 30, message = "Phone is too long")
    private String phone;

    @Size(max = 500, message = "Address is too long")
    private String address;
}
