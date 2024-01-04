package com.tc.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCargoTransportRequest(String startAddress, String endAddress, LocalDate startDate,
        LocalDate endDate, String cargoType, Integer cargoWeight, BigDecimal price, Boolean isPaid, Long customerId,
        Long vehicleId, Long driverId) {
}
