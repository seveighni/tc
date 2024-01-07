package com.tc.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateQualificationRequest {
    @NotBlank(message = "type: must not be empty")
    @NotNull(message = "type: must not be null")
    @Size(min = 1, max = 50, message = "type: must have length between 1 and 50 characters")
    public String type;
}