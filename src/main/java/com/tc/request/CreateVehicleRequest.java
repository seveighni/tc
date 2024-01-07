package com.tc.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateVehicleRequest {
    @NotBlank(message = "registration: must not be empty")
    @NotNull(message = "registration: must not be null")
    @Size(min = 1, max = 50, message = "registration: must have length between 1 and 50 characters")
    public String registration;

    @NotBlank(message = "type: must not be empty")
    @NotNull(message = "type: must not be null")
    @Pattern(regexp = "^(BUS|TRUCK)$", message = "type: must be one of [BUS, TRUCK]")
    public String type;

    @NotBlank(message = "capacity: must not be empty")
    @NotNull(message = "capacity: must not be null")
    @Positive(message = "capacity: must be positive")
    public Integer capacity;
}