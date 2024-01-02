package com.tc.response;

public record VehicleDetailedResponse(Long id, String registration, String type, Integer capacity, CompanyResponse company) {
}