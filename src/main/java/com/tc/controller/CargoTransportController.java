package com.tc.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tc.model.CargoTransport;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CustomerRepository;
import com.tc.repository.DriverRepository;
import com.tc.repository.PassengerTransportRepository;
import com.tc.repository.CargoTransportRepository;
import com.tc.repository.VehicleRepository;
import com.tc.request.CreateCargoTransportRequest;
import com.tc.request.UpdateCargoTransportRequest;
import com.tc.response.CargoTransportResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CargoTransport")
@RestController
@RequestMapping("/api")
public class CargoTransportController {
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CargoTransportRepository cargoTransportRepository;

    public CargoTransportController(CompanyRepository companyRepository,
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
                    transport.getIsPaid(),
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
                    saved.getIsPaid(),
                    saved.getCustomer().getId(),
                    saved.getVehicle().getId(),
                    saved.getDriver().getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cargotransport")
    public ResponseEntity<List<CargoTransportResponse>> getAllCargoTransport() {
        var cargoTransport = cargoTransportRepository.findAll();
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
                    transport.getIsPaid(),
                    transport.getCustomer().getId(),
                    transport.getVehicle().getId(),
                    transport.getDriver().getId());
        }).toList();
        return new ResponseEntity<>(cargoTransportResponse, HttpStatus.OK);
    }

    @GetMapping("/cargotransport/{id}")
    public ResponseEntity<CargoTransportResponse> getCargoTransportById(@PathVariable("id") Long id) {
        var cargoTransportOpt = cargoTransportRepository.findById(id);
        if (!cargoTransportOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var cargoTransport = cargoTransportOpt.get();
        var response = new CargoTransportResponse(
                cargoTransport.getId(),
                cargoTransport.getStartAddress(),
                cargoTransport.getEndAddress(),
                cargoTransport.getStartDate(),
                cargoTransport.getEndDate(),
                cargoTransport.getCargoType(),
                cargoTransport.getCargoWeight(),
                cargoTransport.getPrice(),
                cargoTransport.getIsPaid(),
                cargoTransport.getCustomer().getId(),
                cargoTransport.getVehicle().getId(),
                cargoTransport.getDriver().getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/cargotransport/{id}")
    public ResponseEntity<HttpStatus> deleteCargoTransport(@PathVariable("id") Long id) {
        cargoTransportRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/cargotransport/{id}")
    public ResponseEntity<CargoTransportResponse> updateCargoTransport(@PathVariable("id") Long id,
            @RequestBody UpdateCargoTransportRequest request) {
        try {
            var cargoTransportOpt = cargoTransportRepository.findById(id);
            if (!cargoTransportOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var cargoTransport = cargoTransportOpt.get();
            var companyOpt = companyRepository.findById(cargoTransport.getCompany().getId());
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

            var update = new CargoTransport(
                    request.startAddress(),
                    request.endAddress(),
                    request.startDate(),
                    request.endDate(),
                    request.cargoType(),
                    request.cargoWeight(),
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

            var updated = cargoTransportRepository.save(update);
            var response = new CargoTransportResponse(
                    updated.getId(),
                    updated.getStartAddress(),
                    updated.getEndAddress(),
                    updated.getStartDate(),
                    updated.getEndDate(),
                    updated.getCargoType(),
                    updated.getCargoWeight(),
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
