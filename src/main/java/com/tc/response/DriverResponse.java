package com.tc.response;

import java.math.BigDecimal;

public record DriverResponse(Long id, String firstName, String lastName, BigDecimal salary) {
}
