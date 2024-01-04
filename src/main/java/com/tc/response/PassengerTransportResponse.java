package com.tc.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PassengerTransportResponse(
        Long id,
        String startAddress,
        String endAddress,
        LocalDate startDate,
        LocalDate endDate,
        Integer numberOfPassengers,
        BigDecimal price,
        Boolean isPaid,
        Long customerId,
        Long vehicleId,
        Long driverId) {
}