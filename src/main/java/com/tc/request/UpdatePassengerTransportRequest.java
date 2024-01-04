package com.tc.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePassengerTransportRequest (String startAddress, String endAddress, LocalDate startDate,
        LocalDate endDate, Integer numberOfPassengers, BigDecimal price, Boolean isPaid, Long customerId, Long vehicleId,
        Long driverId) {
}
