package com.tc.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdatePassengerTransportRequest {
        @NotBlank(message = "startAddress: must not be empty")
        @NotNull(message = "startAddress: must not be null")
        @Size(min = 1, max = 100, message = "startAddress: must have length between 1 and 100 characters")
        public String startAddress;

        @NotBlank(message = "endAddress: must not be empty")
        @NotNull(message = "endAddress: must not be null")
        @Size(min = 1, max = 100, message = "endAddress: must have length between 1 and 100 characters")
        public String endAddress;

        @NotNull(message = "startDate: must not be null")
        public LocalDate startDate;

        @NotNull(message = "endDate: must not be null")
        public LocalDate endDate;

        @NotNull(message = "numberOfPassengers: must not be null")
        @Positive(message = "numberOfPassengers: must be positive")
        public Integer numberOfPassengers;

        @NotNull(message = "price: must not be null")
        @Positive(message = "price: must be positive")
        public BigDecimal price;

        @NotNull(message = "isPaid: must not be null")
        public Boolean isPaid;

        @NotNull(message = "customerId: must not be null")
        public Long customerId;

        @NotNull(message = "vehicleId: must not be null")
        public Long vehicleId;

        @NotNull(message = "driverId: must not be null")
        public Long driverId;
}