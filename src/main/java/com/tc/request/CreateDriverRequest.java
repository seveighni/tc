package com.tc.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateDriverRequest {
    @NotBlank(message = "firstName: must not be empty")
    @NotNull(message = "firstName: must not be null")
    @Size(min = 1, max = 50, message = "firstName: must have length between 1 and 50 characters")
    public String firstName;

    @NotBlank(message = "lastName: must not be empty")
    @NotNull(message = "lastName: must not be null")
    @Size(min = 1, max = 50, message = "lastName: must have length between 1 and 50 characters")
    public String lastName;

    @NotBlank(message = "salary: must not be empty")
    @NotNull(message = "salary: must not be null")
    @Positive(message = "salary: must be positive")
    public BigDecimal salary;
}