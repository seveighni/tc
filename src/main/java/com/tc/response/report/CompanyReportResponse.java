package com.tc.response.report;

import java.math.BigDecimal;
import java.util.List;

public record CompanyReportResponse(
                Integer totalFinishedPassengerTransports,
                Integer totalFinishedCargoTransports,
                BigDecimal totalPaidSum,
                List<TransportRef> unpaidTransports) {
}