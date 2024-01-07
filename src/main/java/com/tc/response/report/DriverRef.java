package com.tc.response.report;

import java.math.BigDecimal;

public record DriverRef(Long id, String firstName, String lastName, Long totalCompletedTransports,
        BigDecimal generatedRevenue) {

}
