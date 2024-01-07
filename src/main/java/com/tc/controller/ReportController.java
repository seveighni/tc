package com.tc.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tc.exception.NotFoundException;
import com.tc.model.CargoTransport;
import com.tc.model.PassengerTransport;
import com.tc.repository.CargoTransportRepository;
import com.tc.repository.CompanyRepository;
import com.tc.repository.PassengerTransportRepository;
import com.tc.response.report.CompanyReportResponse;
import com.tc.response.report.DriverRef;
import com.tc.response.report.TransportRef;
import com.tc.specification.TransportSpecification;
import com.tc.util.Util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.annotations.OpenAPI30;

@Tag(name = "Report")
@RestController
@RequestMapping("/api")
public class ReportController {
        private final CompanyRepository companyRepository;
        private final CargoTransportRepository cargoTransportRepository;
        private final PassengerTransportRepository passengerTransportRepository;

        public ReportController(CompanyRepository companyRepository,
                        CargoTransportRepository cargoTransportRepository,
                        PassengerTransportRepository passengerTransportRepository) {
                this.companyRepository = companyRepository;
                this.cargoTransportRepository = cargoTransportRepository;
                this.passengerTransportRepository = passengerTransportRepository;
        }

        @Operation(summary = "Retrieve a report for a company")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The report was retrieved"),
                        @ApiResponse(responseCode = "404", description = "The company was not found") })
        @GetMapping("/report/companies/{companyId}")
        public ResponseEntity<CompanyReportResponse> getCompanyReport(
                        @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
                        @Parameter(description = "the start date to consider when gathering data for the report") @RequestParam(required = false) LocalDate fromDate,
                        @Parameter(description = "the end date to consider when gathering data for the report") @RequestParam(required = false) LocalDate toDate) {
                var company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException(
                                "company not found"));
                var startDate = fromDate == null ? LocalDate.of(1970, 1, 1) : fromDate;
                var endDate = toDate == null ? LocalDate.now() : toDate;

                Specification<PassengerTransport> specPassengerInDateRange = TransportSpecification.endDateInRange(
                                startDate,
                                endDate);
                Specification<PassengerTransport> specPassengerHasCompanyId = TransportSpecification
                                .hasCompanyId(company.getId());
                var passengerTransports = passengerTransportRepository.findAll(Specification
                                .where(specPassengerInDateRange)
                                .and(specPassengerHasCompanyId));

                Specification<CargoTransport> specCargoInDateRange = TransportSpecification.endDateInRange(startDate,
                                endDate);
                Specification<CargoTransport> specCargoHasCompanyId = TransportSpecification
                                .hasCompanyId(company.getId());
                var cargoTransports = cargoTransportRepository.findAll(Specification
                                .where(specCargoInDateRange)
                                .and(specCargoHasCompanyId));

                var unpaidTransports = Stream
                                .concat(passengerTransports.stream().filter(transport -> !transport.getIsPaid())
                                                .map(transport -> {
                                                        return new TransportRef(transport.getId(), "passenger");
                                                }), cargoTransports.stream().filter(transport -> !transport.getIsPaid())
                                                                .map(transport -> {
                                                                        return new TransportRef(transport.getId(),
                                                                                        "cargo");
                                                                }))
                                .toList();

                var totalRevenue = Stream.concat(passengerTransports.stream().filter(transport -> transport.getIsPaid())
                                .map(transport -> transport.getPrice()),
                                cargoTransports.stream().filter(transport -> transport.getIsPaid())
                                                .map(transport -> transport.getPrice()))
                                .reduce(BigDecimal.ZERO,
                                                BigDecimal::add);

                var drivers = Stream.concat(passengerTransports.stream().map(transport -> transport.getDriver()),
                                cargoTransports.stream().map(transport -> transport.getDriver()))
                                .filter(Util.distinctByKey(d -> d.getId())).toList();

                List<DriverRef> driversReport = drivers.stream().map(driver -> {
                        var completedPassengerTransports = passengerTransports.stream()
                                        .filter(transport -> transport.getDriver().getId().equals(
                                                        driver.getId()))
                                        .count();
                        var completedCargoTransports = cargoTransports.stream()
                                        .filter(transport -> transport.getDriver().getId().equals(driver.getId()))
                                        .count();

                        var generatedRevenue = Stream.concat(
                                        passengerTransports.stream()
                                                        .filter(transport -> transport.getDriver().getId().equals(
                                                                        driver.getId()) && transport.getIsPaid())
                                                        .map(transport -> transport.getPrice()),
                                        cargoTransports.stream()
                                                        .filter(transport -> transport.getDriver()
                                                                        .getId().equals(driver.getId())
                                                                        && transport.getIsPaid())
                                                        .map(transport -> transport.getPrice()))
                                        .reduce(BigDecimal.ZERO,
                                                        BigDecimal::add);
                        return new DriverRef(driver.getId(), driver.getFirstName(), driver.getLastName(),
                                        completedPassengerTransports + completedCargoTransports,
                                        generatedRevenue);
                }).toList();

                var response = new CompanyReportResponse(
                                passengerTransports.size(),
                                cargoTransports.size(),
                                totalRevenue,
                                unpaidTransports,
                                driversReport);
                return new ResponseEntity<>(response, HttpStatus.OK);
        }
}
