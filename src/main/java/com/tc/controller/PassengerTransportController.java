package com.tc.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tc.model.PassengerTransport;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CustomerRepository;
import com.tc.repository.DriverRepository;
import com.tc.repository.PassengerTransportRepository;
import com.tc.repository.CargoTransportRepository;
import com.tc.repository.VehicleRepository;
import com.tc.request.CreatePassengerTransportRequest;
import com.tc.request.UpdatePassengerTransportRequest;
import com.tc.response.PassengerTransportResponse;
import com.tc.specification.TransportSpecification;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PassengerTransport")
@RestController
@RequestMapping("/api")
public class PassengerTransportController {
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CargoTransportRepository cargoTransportRepository;
    private final PassengerTransportRepository passengerTransportRepository;

    public PassengerTransportController(CompanyRepository companyRepository,
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

    @GetMapping("/companies/{companyId}/passengertransport")
    public ResponseEntity<List<PassengerTransportResponse>> getPassengerTransportByCompanyId(
            @PathVariable("companyId") Long companyId,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "0") int page) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Specification<PassengerTransport> hasCompanyId = TransportSpecification.hasCompanyId(companyId);
        Specification<PassengerTransport> filters = Specification
                .where(hasCompanyId)
                .and(destination == null ? null : TransportSpecification.hasDestination(destination));

        var passengerTransport = passengerTransportRepository.findAll(filters, PageRequest.of(page, 20));
        var passengerTransportResponse = passengerTransport.stream().map(transport -> {
            return new PassengerTransportResponse(
                    transport.getId(),
                    transport.getStartAddress(),
                    transport.getEndAddress(),
                    transport.getStartDate(),
                    transport.getEndDate(),
                    transport.getNumberOfPassengers(),
                    transport.getPrice(),
                    transport.getIsPaid(),
                    transport.getCustomer().getId(),
                    transport.getVehicle().getId(),
                    transport.getDriver().getId());
        }).toList();
        return new ResponseEntity<>(passengerTransportResponse, HttpStatus.OK);
    }

    @PostMapping("/companies/{companyId}/passengertransport")
    public ResponseEntity<PassengerTransportResponse> createPassengerTransport(
            @PathVariable("companyId") Long companyId,
            @RequestBody CreatePassengerTransportRequest request) {
        try {
            var companyOpt = companyRepository.findById(companyId);
            if (!companyOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var company = companyOpt.get();

            var driverOpt = company.getDrivers().stream()
                    .filter(driver -> driver.getId() == request.driverId()).findFirst();
            if (!driverOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            var customerOpt = company.getCustomers().stream()
                    .filter(customer -> customer.getId() == request.customerId()).findFirst();
            if (!customerOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            var vehicleOpt = company.getVehicles().stream()
                    .filter(vehicle -> vehicle.getId() == request.vehicleId()).findFirst();
            if (!vehicleOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            var passengerTransport = new PassengerTransport(
                    request.startAddress(),
                    request.endAddress(),
                    request.startDate(),
                    request.endDate(),
                    request.numberOfPassengers(),
                    request.price(),
                    false);
            var customer = customerOpt.get();
            var vehicle = vehicleOpt.get();
            var driver = driverOpt.get();
            passengerTransport.setCompany(company);
            passengerTransport.setCustomer(customer);
            passengerTransport.setDriver(driver);
            passengerTransport.setVehicle(vehicle);
            var saved = this.passengerTransportRepository.save(passengerTransport);
            var response = new PassengerTransportResponse(
                    saved.getId(),
                    saved.getStartAddress(),
                    saved.getEndAddress(),
                    saved.getStartDate(),
                    saved.getEndDate(),
                    saved.getNumberOfPassengers(),
                    saved.getPrice(),
                    saved.getIsPaid(),
                    saved.getCustomer().getId(),
                    saved.getVehicle().getId(),
                    saved.getDriver().getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/passengertransport/{id}")
    public ResponseEntity<PassengerTransportResponse> getPassengerTransportById(@PathVariable("id") Long id) {
        var passengerTransportOpt = passengerTransportRepository.findById(id);
        if (!passengerTransportOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var passengerTransport = passengerTransportOpt.get();
        var response = new PassengerTransportResponse(
                passengerTransport.getId(),
                passengerTransport.getStartAddress(),
                passengerTransport.getEndAddress(),
                passengerTransport.getStartDate(),
                passengerTransport.getEndDate(),
                passengerTransport.getNumberOfPassengers(),
                passengerTransport.getPrice(),
                passengerTransport.getIsPaid(),
                passengerTransport.getCustomer().getId(),
                passengerTransport.getVehicle().getId(),
                passengerTransport.getDriver().getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/passengertransport/{id}")
    public ResponseEntity<HttpStatus> deletePassengerTransport(@PathVariable("id") Long id) {
        passengerTransportRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/passengertransport/{id}")
    public ResponseEntity<PassengerTransportResponse> updatePassengerTransport(@PathVariable("id") Long id,
            @RequestBody UpdatePassengerTransportRequest request) {
        try {
            var passengerTransportOpt = passengerTransportRepository.findById(id);
            if (!passengerTransportOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var passengerTransport = passengerTransportOpt.get();
            var companyOpt = companyRepository.findById(passengerTransport.getCompany().getId());
            if (!companyOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var company = companyOpt.get();

            var driverOpt = company.getDrivers().stream()
                    .filter(driver -> driver.getId() == request.driverId()).findFirst();
            if (!driverOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            var customerOpt = company.getCustomers().stream()
                    .filter(customer -> customer.getId() == request.customerId()).findFirst();
            if (!customerOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            var vehicleOpt = company.getVehicles().stream()
                    .filter(vehicle -> vehicle.getId() == request.vehicleId()).findFirst();
            if (!vehicleOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            var update = new PassengerTransport(
                    request.startAddress(),
                    request.endAddress(),
                    request.startDate(),
                    request.endDate(),
                    request.numberOfPassengers(),
                    request.price(),
                    request.isPaid());
            var customer = customerOpt.get();
            var vehicle = vehicleOpt.get();
            var driver = driverOpt.get();
            update.setId(id);
            update.setCompany(company);
            update.setCustomer(customer);
            update.setDriver(driver);
            update.setVehicle(vehicle);

            var updated = passengerTransportRepository.save(update);
            var response = new PassengerTransportResponse(
                    updated.getId(),
                    updated.getStartAddress(),
                    updated.getEndAddress(),
                    updated.getStartDate(),
                    updated.getEndDate(),
                    updated.getNumberOfPassengers(),
                    updated.getPrice(),
                    updated.getIsPaid(),
                    updated.getCustomer().getId(),
                    updated.getVehicle().getId(),
                    updated.getDriver().getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
