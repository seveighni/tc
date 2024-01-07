package com.tc.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateCargoTransportRequest {
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

        @NotBlank(message = "cargoType: must not be empty")
        @NotNull(message = "cargoType: must not be null")
        @Size(min = 1, max = 50, message = "cargoType: must have length between 1 and 50 characters")
        public String cargoType;

        @NotNull(message = "cargoWeight: must not be null")
        @Positive(message = "cargoWeight: must be positive")
        public Integer cargoWeight;

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