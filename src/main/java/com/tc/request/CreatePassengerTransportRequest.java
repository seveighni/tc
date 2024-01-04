package com.tc.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePassengerTransportRequest(String startAddress, String endAddress, LocalDate startDate,
        LocalDate endDate, Integer numberOfPassengers, BigDecimal price, Long customerId, Long vehicleId,
        Long driverId) {
}
