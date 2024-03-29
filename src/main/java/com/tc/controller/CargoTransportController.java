package com.tc.controller;

import java.util.List;

import org.javatuples.Quartet;
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

import com.tc.exception.BadRequestException;
import com.tc.exception.NotFoundException;
import com.tc.model.CargoTransport;
import com.tc.model.Company;
import com.tc.model.Customer;
import com.tc.model.Driver;
import com.tc.model.Vehicle;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CargoTransportRepository;
import com.tc.request.CreateCargoTransportRequest;
import com.tc.request.UpdateCargoTransportRequest;
import com.tc.response.CargoTransportResponse;
import com.tc.specification.TransportSpecification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "CargoTransport")
@RestController
@RequestMapping("/api")
public class CargoTransportController {
        private final CompanyRepository companyRepository;
        private final CargoTransportRepository cargoTransportRepository;

        public CargoTransportController(CompanyRepository companyRepository,
                        CargoTransportRepository cargoTransportRepository) {
                this.companyRepository = companyRepository;
                this.cargoTransportRepository = cargoTransportRepository;
        }

        @Operation(summary = "Retrieve cargo transports of a company")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The cargo transports were retrieved"),
                        @ApiResponse(responseCode = "404", description = "The company was not found"), })
        @GetMapping("/companies/{companyId}/cargotransport")
        public ResponseEntity<List<CargoTransportResponse>> getCargoTransportByCompanyId(
                        @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
                        @Parameter(description = "the transport destination") @RequestParam(required = false) String destination,
                        @Parameter(description = "the requested page") @RequestParam(defaultValue = "0") int page) {
                var company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new NotFoundException("company not found"));
                Specification<CargoTransport> hasCompanyId = TransportSpecification.hasCompanyId(company.getId());
                Specification<CargoTransport> filters = Specification
                                .where(hasCompanyId)
                                .and(destination == null ? null : TransportSpecification.hasDestination(destination));
                var cargoTransport = cargoTransportRepository.findAll(filters, PageRequest.of(page, 20));
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

        @Operation(summary = "Create a new cargo transport")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "A new cargo transport was created"),
                        @ApiResponse(responseCode = "400", description = "The request body was invalid"),
                        @ApiResponse(responseCode = "404", description = "Some of the referenced entities were not found") })
        @PostMapping("/companies/{companyId}/cargotransport")
        public ResponseEntity<CargoTransportResponse> createCargoTransport(
                        @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
                        @Parameter(description = "the create parameters") @RequestBody @Valid CreateCargoTransportRequest request) {
                var references = getReferences(companyId, request.driverId, request.customerId, request.vehicleId);
                var company = references.getValue0();
                var driver = references.getValue1();
                var customer = references.getValue2();
                var vehicle = references.getValue3();

                if (!vehicle.getType().equals("TRUCK")) {
                        throw new BadRequestException("cargo transport can be done only by truck");
                }

                if (request.cargoWeight > vehicle.getCapacity()) {
                        throw new BadRequestException("cargo weight exceeds vehicle capacity");
                }

                if (request.startDate.isAfter(request.endDate)) {
                        throw new BadRequestException("start date cannot be after end date");
                }

                var cargoTransport = new CargoTransport(
                                request.startAddress,
                                request.endAddress,
                                request.startDate,
                                request.endDate,
                                request.cargoType,
                                request.cargoWeight,
                                request.price,
                                false);
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
        }

        @Operation(summary = "Retrieve a cargo transport by its id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The cargo transport was retrieved"),
                        @ApiResponse(responseCode = "404", description = "The cargo transport was not found"), })
        @GetMapping("/cargotransport/{id}")
        public ResponseEntity<CargoTransportResponse> getCargoTransportById(
                        @Parameter(description = "the id of the cargo transport") @PathVariable("id") Long id) {
                var cargoTransport = cargoTransportRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("cargo transport not found"));
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

        @Operation(summary = "Delete a cargo transport")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "The cargo transport was deleted or does not exist"), })
        @DeleteMapping("/cargotransport/{id}")
        public ResponseEntity<HttpStatus> deleteCargoTransport(@PathVariable("id") Long id) {
                cargoTransportRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        @Operation(summary = "Update a cargo transport")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The cargo transport was updated"),
                        @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
                        @ApiResponse(responseCode = "404", description = "The cargo transport or some of the referenced entities were not found") })
        @PutMapping("/cargotransport/{id}")
        public ResponseEntity<CargoTransportResponse> updateCargoTransport(
                        @Parameter(description = "the id of the cargo transport") @PathVariable("id") Long id,
                        @Parameter(description = "the update parameters") @RequestBody @Valid UpdateCargoTransportRequest request) {
                var cargoTransport = cargoTransportRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("cargo transport not found"));
                var references = getReferences(cargoTransport.getCompany().getId(), request.driverId,
                                request.customerId,
                                request.vehicleId);
                var company = references.getValue0();
                var driver = references.getValue1();
                var customer = references.getValue2();
                var vehicle = references.getValue3();

                if (!vehicle.getType().equals("TRUCK")) {
                        throw new BadRequestException("cargo transport can be done only by truck");
                }

                if (request.cargoWeight > vehicle.getCapacity()) {
                        throw new BadRequestException("cargo weight exceeds vehicle capacity");
                }

                if (request.startDate.isAfter(request.endDate)) {
                        throw new BadRequestException("start date cannot be after end date");
                }

                var update = new CargoTransport(
                                request.startAddress,
                                request.endAddress,
                                request.startDate,
                                request.endDate,
                                request.cargoType,
                                request.cargoWeight,
                                request.price,
                                request.isPaid);

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
        }

        private Quartet<Company, Driver, Customer, Vehicle> getReferences(Long companyId, Long driverId,
                        Long customerId,
                        Long vehicleId) {
                var company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new NotFoundException("company not found"));

                var driver = company.getDrivers().stream()
                                .filter(d -> d.getId() == driverId).findFirst()
                                .orElseThrow(() -> new BadRequestException("no such driver working for the company"));

                var customer = company.getCustomers().stream()
                                .filter(c -> c.getId() == customerId).findFirst().orElseThrow(
                                                () -> new BadRequestException("the company has no such customer"));

                var vehicle = company.getVehicles().stream()
                                .filter(v -> v.getId() == vehicleId).findFirst().orElseThrow(
                                                () -> new BadRequestException("the company does not own such vehicle"));

                return new Quartet<>(company, driver, customer, vehicle);
        }
}
