package com.tc.response;

import java.util.List;

public record CustomerDetailedResponse(Long id, String name, List<CompanyResponse> company) {
}