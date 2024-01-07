package com.tc.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCustomerRequest {
    public Long id;

    @NotBlank(message = "name: must not be empty")
    @NotNull(message = "name: must not be null")
    @Size(min = 1, max = 50, message = "name: must have length between 1 and 50 characters")
    public String name;
}