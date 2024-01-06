package com.tc.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tc.model.CargoTransport;
import com.tc.model.PassengerTransport;
import com.tc.repository.CargoTransportRepository;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CustomerRepository;
import com.tc.repository.DriverRepository;
import com.tc.repository.PassengerTransportRepository;
import com.tc.repository.VehicleRepository;
import com.tc.response.report.CompanyReportResponse;
import com.tc.response.report.TransportRef;
import com.tc.specification.TransportSpecification;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Report")
@RestController
@RequestMapping("/api")
public class ReportController {
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CargoTransportRepository cargoTransportRepository;
    private final PassengerTransportRepository passengerTransportRepository;

    public ReportController(CompanyRepository companyRepository,
            DriverRepository driverRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            CargoTransportRepository cargoTransportRepository,
            PassengerTransportRepository passengerTransportRepository) {
        this.companyRepository = companyRepository;
        this.driverRepository = driverRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.cargoTransportRepository = cargoTransportRepository;
        this.passengerTransportRepository = passengerTransportRepository;
    }

    @GetMapping("/report/companies/{companyId}")
    public ResponseEntity<CompanyReportResponse> getCompanyReport(
            @PathVariable("companyId") Long companyId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var startDate = fromDate == null ? LocalDate.of(1970, 1, 1) : fromDate;
        var endDate = toDate == null ? LocalDate.now() : toDate;

        Specification<PassengerTransport> specPassengerInDateRange = TransportSpecification.endDateInRange(startDate,
                endDate);
        Specification<PassengerTransport> specPassengerHasCompanyId = TransportSpecification.hasCompanyId(companyId);
        var passengerTransports = passengerTransportRepository.findAll(Specification
                .where(specPassengerInDateRange)
                .and(specPassengerHasCompanyId));

        Specification<CargoTransport> specCargoInDateRange = TransportSpecification.endDateInRange(startDate,
                endDate);
        Specification<CargoTransport> specCargoHasCompanyId = TransportSpecification.hasCompanyId(companyId);
        var cargoTransports = cargoTransportRepository.findAll(Specification
                .where(specCargoInDateRange)
                .and(specCargoHasCompanyId));

        var unpaidTransports = Stream
                .concat(passengerTransports.stream().filter(transport -> !transport.getIsPaid()).map(transport -> {
                    return new TransportRef(transport.getId(), "passenger");
                }), cargoTransports.stream().filter(transport -> !transport.getIsPaid()).map(transport -> {
                    return new TransportRef(transport.getId(), "cargo");
                })).toList();

        var totalPaidSum = Stream.concat(passengerTransports.stream().filter(transport -> transport.getIsPaid())
                .map(transport -> transport.getPrice()),
                cargoTransports.stream().filter(transport -> transport.getIsPaid())
                        .map(transport -> transport.getPrice()))
                .reduce(BigDecimal.ZERO,
                        BigDecimal::add);

        var response = new CompanyReportResponse(
                passengerTransports.size(),
                cargoTransports.size(),
                totalPaidSum,
                unpaidTransports);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
