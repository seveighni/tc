package com.tc.response.report;

import java.math.BigDecimal;
import java.util.List;

public record CompanyReportResponse(
                Integer totalFinishedPassengerTransports,
                Integer totalFinishedCargoTransports,
                BigDecimal totalRevenue,
                List<TransportRef> unpaidTransports,
                List<DriverRef> drivers) {
}