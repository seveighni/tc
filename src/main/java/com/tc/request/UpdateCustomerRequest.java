package com.tc.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateCustomerRequest {
    @NotBlank(message = "name: must not be empty")
    @NotNull(message = "name: must not be null")
    @Size(min = 3, max = 50, message = "name: must have length between 3 and 50 characters")
    public String name;
}