package com.tc.response;

import java.math.BigDecimal;
import java.util.List;

public record DriverDetailedResponse(Long id, String firstName, String lastName, BigDecimal salary,
        CompanyResponse company, List<QualificationResponse> qualifications) {
}
