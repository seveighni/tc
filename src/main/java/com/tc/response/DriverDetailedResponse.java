package com.tc.response;

import java.util.List;

public record DriverDetailedResponse(Long id, String firstName, String lastName, CompanyResponse company, List<QualificationResponse> qualifications) {
}
