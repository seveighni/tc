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
import com.tc.model.Company;
import com.tc.model.Customer;
import com.tc.model.Driver;
import com.tc.model.PassengerTransport;
import com.tc.model.Vehicle;
import com.tc.repository.CompanyRepository;
import com.tc.repository.PassengerTransportRepository;
import com.tc.request.CreatePassengerTransportRequest;
import com.tc.request.UpdatePassengerTransportRequest;
import com.tc.response.PassengerTransportResponse;
import com.tc.specification.TransportSpecification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "PassengerTransport")
@RestController
@RequestMapping("/api")
public class PassengerTransportController {
        private final CompanyRepository companyRepository;
        private final PassengerTransportRepository passengerTransportRepository;

        public PassengerTransportController(CompanyRepository companyRepository,
                        PassengerTransportRepository passengerTransportRepository) {
                this.companyRepository = companyRepository;
                this.passengerTransportRepository = passengerTransportRepository;
        }

        @Operation(summary = "Retrieve passenger transports of a company")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The passenger transports were retrieved"),
                        @ApiResponse(responseCode = "404", description = "The company was not found"), })
        @GetMapping("/companies/{companyId}/passengertransport")
        public ResponseEntity<List<PassengerTransportResponse>> getPassengerTransportByCompanyId(
                        @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
                        @Parameter(description = "the transport destination") @RequestParam(required = false) String destination,
                        @Parameter(description = "the requested page") @RequestParam(defaultValue = "0") int page) {
                var company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new NotFoundException("company not found"));
                Specification<PassengerTransport> hasCompanyId = TransportSpecification.hasCompanyId(company.getId());
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

        @Operation(summary = "Create a new passenger transport")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "A new passenger transport was created"),
                        @ApiResponse(responseCode = "400", description = "The request body was invalid"),
                        @ApiResponse(responseCode = "404", description = "The company, driver, customer or vehicle was not found") })
        @PostMapping("/companies/{companyId}/passengertransport")
        public ResponseEntity<PassengerTransportResponse> createPassengerTransport(
                        @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
                        @Parameter(description = "the create parameters") @RequestBody @Valid CreatePassengerTransportRequest request) {
                var references = getReferences(companyId, request.driverId, request.customerId, request.vehicleId);
                var company = references.getValue0();
                var driver = references.getValue1();
                var customer = references.getValue2();
                var vehicle = references.getValue3();

                if (!vehicle.getType().equals("BUS")) {
                        throw new BadRequestException("passenger transport can be done only by bus");
                }

                if (request.numberOfPassengers > vehicle.getCapacity()) {
                        throw new BadRequestException("vehicle capacity is not enough to carry all passengers");
                }

                if (request.startDate.isAfter(request.endDate)) {
                        throw new BadRequestException("start date cannot be after end date");
                }

                var passengerTransport = new PassengerTransport(
                                request.startAddress,
                                request.endAddress,
                                request.startDate,
                                request.endDate,
                                request.numberOfPassengers,
                                request.price,
                                false);

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
        }

        @Operation(summary = "Retrieve a passenger transport by its id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The passenger transport was retrieved"),
                        @ApiResponse(responseCode = "404", description = "The passenger transport was not found") })
        @GetMapping("/passengertransport/{id}")
        public ResponseEntity<PassengerTransportResponse> getPassengerTransportById(
                        @Parameter(description = "the id of the passenger transport") @PathVariable("id") Long id) {
                var passengerTransport = passengerTransportRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("passenger transport not found"));
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

        @Operation(summary = "Delete a passenger transport")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "The passenger transport was deleted or does not exist"), })
        @DeleteMapping("/passengertransport/{id}")
        public ResponseEntity<HttpStatus> deletePassengerTransport(@PathVariable("id") Long id) {
                passengerTransportRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        @Operation(summary = "Update a passenger transport")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The passenger transport was updated"),
                        @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
                        @ApiResponse(responseCode = "404", description = "The passenger transport or some of the referenced entities were not found") })
        @PutMapping("/passengertransport/{id}")
        public ResponseEntity<PassengerTransportResponse> updatePassengerTransport(
                        @Parameter(description = "the id of the passenger transport") @PathVariable("id") Long id,
                        @Parameter(description = "the update parameters") @RequestBody @Valid UpdatePassengerTransportRequest request) {
                var passengerTransport = passengerTransportRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("passenger transport not found"));
                var reference = getReferences(passengerTransport.getCompany().getId(), request.driverId,
                                request.customerId,
                                request.vehicleId);
                var company = reference.getValue0();
                var driver = reference.getValue1();
                var customer = reference.getValue2();
                var vehicle = reference.getValue3();

                if (!vehicle.getType().equals("BUS")) {
                        throw new BadRequestException("passenger transport can be done only by bus");
                }

                if (request.numberOfPassengers > vehicle.getCapacity()) {
                        throw new BadRequestException("vehicle capacity is not enough to carry all passengers");
                }

                if (request.startDate.isAfter(request.endDate)) {
                        throw new BadRequestException("start date cannot be after end date");
                }

                var update = new PassengerTransport(
                                request.startAddress,
                                request.endAddress,
                                request.startDate,
                                request.endDate,
                                request.numberOfPassengers,
                                request.price,
                                request.isPaid);

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
