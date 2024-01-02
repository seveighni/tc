package com.tc.response;

public record DriverDetailedResponse(Long id, String firstName, String lastName, CompanyResponse company) {
}
