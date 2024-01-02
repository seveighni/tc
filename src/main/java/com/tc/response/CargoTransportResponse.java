package com.tc.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CargoTransportResponse (
    Long id,
    String startAddress,
    String endAddress,
    LocalDate startDate,
    LocalDate endDate,
    String cargoType,
    Integer cargoWeight,
    BigDecimal price,
    Boolean isPayed,
    Long customerId,
    Long vehicleId,
    Long employeeId
) {
}
