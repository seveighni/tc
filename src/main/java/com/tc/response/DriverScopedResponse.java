package com.tc.response;

import java.math.BigDecimal;
import java.util.List;

public record DriverScopedResponse(Long id, String firstName, String lastName, BigDecimal salary, List<QualificationResponse> qualifications) {
}
