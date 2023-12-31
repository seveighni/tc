package com.tc.response;

public record EmployeeDetailedResponse(Long id, String firstName, String lastName, CompanyResponse company) {
}
