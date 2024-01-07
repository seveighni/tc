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

import com.tc.exception.NotFoundException;
import com.tc.model.Vehicle;
import com.tc.repository.CompanyRepository;
import com.tc.repository.VehicleRepository;
import com.tc.request.CreateVehicleRequest;
import com.tc.request.UpdateVehicleRequest;
import com.tc.response.CompanyResponse;
import com.tc.response.VehicleDetailedResponse;
import com.tc.response.VehicleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Vehicle")
@RestController
@RequestMapping("/api")
public class VehicleController {
    private final CompanyRepository companyRepository;
    private final VehicleRepository vehicleRepository;

    public VehicleController(CompanyRepository companyRepository, VehicleRepository vehicleRepository) {
        this.companyRepository = companyRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Operation(summary = "Retrieve vehicles of a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The vehicles were retrieved"),
            @ApiResponse(responseCode = "404", description = "The company was not found") })
    @GetMapping("/companies/{companyId}/vehicles")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByCompanyId(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(company.getId());
        var vehiclesResponse = vehicles.stream().map(vehicle -> {
            return new VehicleResponse(vehicle.getId(), vehicle.getRegistration(), vehicle.getType(),
                    vehicle.getCapacity());
        }).toList();
        return new ResponseEntity<>(vehiclesResponse, HttpStatus.OK);
    }

    @Operation(summary = "Register a vehicle for a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The vehicle was registered"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404", description = "The company was not found") })
    @PostMapping("/companies/{companyId}/vehicles")
    public ResponseEntity<VehicleResponse> registerVehicle(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
            @Parameter(description = "the create parameretes") @RequestBody @Valid CreateVehicleRequest request) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));

        var vehicle = new Vehicle(request.registration, request.type, request.capacity);
        vehicle.setCompany(company);

        var vehicleSaved = vehicleRepository.save(vehicle);

        var response = new VehicleResponse(vehicleSaved.getId(), vehicleSaved.getRegistration(),
                vehicleSaved.getType(), vehicleSaved.getCapacity());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve a vehicle by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The vehicle was retrieved"),
            @ApiResponse(responseCode = "404", description = "The vehicle was not found") })
    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDetailedResponse> getVehicleById(
            @Parameter(description = "the id of the vehicle") @PathVariable("id") Long id) {
        var vehicle = vehicleRepository.findById(id).orElseThrow(() -> new NotFoundException("vehicle not found"));
        var company = vehicle.getCompany();
        var response = new VehicleDetailedResponse(vehicle.getId(), vehicle.getRegistration(), vehicle.getType(),
                vehicle.getCapacity(), new CompanyResponse(company.getId(), company.getName()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The vehicle was updated"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404", description = "The vehicle was not found") })
    @PutMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDetailedResponse> updateVehicle(
            @Parameter(description = "the id of the vehicle") @PathVariable("id") Long id,
            @Parameter(description = "the update parameters") @RequestBody @Valid UpdateVehicleRequest request) {
        var vehicle = vehicleRepository.findById(id).orElseThrow(() -> new NotFoundException("vehicle not found"));
        vehicle.setRegistration(request.registration);
        vehicle.setType(request.type);
        vehicle.setCapacity(request.capacity);
        var vehicleSaved = vehicleRepository.save(vehicle);

        var company = vehicleSaved.getCompany();
        var response = new VehicleDetailedResponse(vehicleSaved.getId(), vehicleSaved.getRegistration(),
                vehicleSaved.getType(), vehicleSaved.getCapacity(),
                new CompanyResponse(company.getId(), company.getName()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Delete a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The vehicle was deleted or does not exist"), })
    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<HttpStatus> deleteVehicle(
            @Parameter(description = "the id of the vehicle") @PathVariable("id") Long id) {
        vehicleRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
