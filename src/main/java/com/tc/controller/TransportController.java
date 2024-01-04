package com.tc.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tc.model.CargoTransport;
import com.tc.model.PassengerTransport;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CustomerRepository;
import com.tc.repository.DriverRepository;
import com.tc.repository.TransportRepository;
import com.tc.repository.VehicleRepository;
import com.tc.request.CreateCargoTransportRequest;
import com.tc.request.CreatePassengerTransportRequest;
import com.tc.response.CargoTransportResponse;
import com.tc.response.PassengerTransportResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Transport")
@RestController
@RequestMapping("/api")
public class TransportController {
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final TransportRepository<CargoTransport> cargoTransportRepository;
    private final TransportRepository<PassengerTransport> passengerTransportRepository;

    public TransportController(CompanyRepository companyRepository,
            DriverRepository driverRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            TransportRepository<CargoTransport> cargoTransportRepository,
            TransportRepository<PassengerTransport> passengerTransportRepository) {
        this.companyRepository = companyRepository;
        this.driverRepository = driverRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.cargoTransportRepository = cargoTransportRepository;
        this.passengerTransportRepository = passengerTransportRepository;
    }

    @GetMapping("/companies/{companyId}/cargotransport")
    public ResponseEntity<List<CargoTransportResponse>> getCargoTransportByCompanyId(
            @PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var cargoTransport = cargoTransportRepository.findByCompanyId(companyId);
        var cargoTransportResponse = cargoTransport.stream().map(transport -> {
            return new CargoTransportResponse(
                    transport.getId(),
                    transport.getStartAddress(),
                    transport.getEndAddress(),
                    transport.getStartDate(),
                    transport.getEndDate(),
                    transport.getCargoType(),
                    transport.getCargoWeight(),
                    transport.getPrice(),
                    transport.getIsPayed(),
                    transport.getCustomer().getId(),
                    transport.getVehicle().getId(),
                    transport.getDriver().getId());
        }).toList();
        return new ResponseEntity<>(cargoTransportResponse, HttpStatus.OK);
    }

    @PostMapping("/companies/{companyId}/cargotransport")
    public ResponseEntity<CargoTransportResponse> createCargoTransport(@PathVariable("companyId") Long companyId,
            @RequestBody CreateCargoTransportRequest request) {
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

            // TODO: validate dates and other fields
            var cargoTransport = new CargoTransport(
                    request.startAddress(),
                    request.endAddress(),
                    request.startDate(),
                    request.endDate(),
                    request.cargoType(),
                    request.cargoWeight(),
                    request.price(),
                    false);
            var customer = customerOpt.get();
            var vehicle = vehicleOpt.get();
            var driver = driverOpt.get();
            cargoTransport.setCompany(company);
            cargoTransport.setCustomer(customer);
            cargoTransport.setDriver(driver);
            cargoTransport.setVehicle(vehicle);
            var saved = this.cargoTransportRepository.save(cargoTransport);
            var response = new CargoTransportResponse(
                    saved.getId(),
                    saved.getStartAddress(),
                    saved.getEndAddress(),
                    saved.getStartDate(),
                    saved.getEndDate(),
                    saved.getCargoType(),
                    saved.getCargoWeight(),
                    saved.getPrice(),
                    saved.getIsPayed(),
                    saved.getCustomer().getId(),
                    saved.getVehicle().getId(),
                    saved.getDriver().getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/companies/{companyId}/passengertransport")
    public ResponseEntity<List<PassengerTransportResponse>> getPassengerTransportByCompanyId(
            @PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var passengerTransport = passengerTransportRepository.findByCompanyId(companyId);
        var passengerTransportResponse = passengerTransport.stream().map(transport -> {
            return new PassengerTransportResponse(
                    transport.getId(),
                    transport.getStartAddress(),
                    transport.getEndAddress(),
                    transport.getStartDate(),
                    transport.getEndDate(),
                    transport.getNumberOfPassengers(),
                    transport.getPrice(),
                    transport.getIsPayed(),
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
                    saved.getIsPayed(),
                    saved.getCustomer().getId(),
                    saved.getVehicle().getId(),
                    saved.getDriver().getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
