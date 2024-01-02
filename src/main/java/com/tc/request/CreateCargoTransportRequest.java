package com.tc.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCargoTransportRequest(String startAddress, String endAddress, LocalDate startDate,
        LocalDate endDate, String cargoType, Integer cargoWeight, BigDecimal price, Long customerId, Long vehicleId, Long employeeId) {
}
