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

import com.tc.model.Vehicle;
import com.tc.repository.CompanyRepository;
import com.tc.repository.VehicleRepository;
import com.tc.request.CreateVehicleRequest;
import com.tc.request.UpdateVehicleRequest;
import com.tc.response.CompanyResponse;
import com.tc.response.VehicleDetailedResponse;
import com.tc.response.VehicleResponse;

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

    @GetMapping("/companies/{companyId}/vehicles")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByCompanyId(@PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        var vehiclesResponse = vehicles.stream().map(vehicle -> {
            return new VehicleResponse(vehicle.getId(), vehicle.getRegistration(), vehicle.getType(),
                    vehicle.getCapacity());
        }).toList();
        return new ResponseEntity<>(vehiclesResponse, HttpStatus.OK);
    }

    @PostMapping("/companies/{companyId}/vehicles")
    public ResponseEntity<VehicleResponse> registerVehicle(@PathVariable("companyId") Long companyId,
            @RequestBody @Valid CreateVehicleRequest request) {
        try {
            var companyOpt = companyRepository.findById(companyId);
            if (!companyOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            var vehicle = new Vehicle(request.registration, request.type, request.capacity);
            vehicle.setCompany(companyOpt.get());

            var vehicleSaved = vehicleRepository.save(vehicle);

            var response = new VehicleResponse(vehicleSaved.getId(), vehicleSaved.getRegistration(),
                    vehicleSaved.getType(), vehicleSaved.getCapacity());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDetailedResponse> getVehicleById(@PathVariable("id") Long id) {
        try {
            var vehicleOpt = vehicleRepository.findById(id);

            if (!vehicleOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var vehicle = vehicleOpt.get();
            var company = vehicle.getCompany();
            var response = new VehicleDetailedResponse(vehicle.getId(), vehicle.getRegistration(), vehicle.getType(),
                    vehicle.getCapacity(), new CompanyResponse(company.getId(), company.getName()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDetailedResponse> updateVehicle(@PathVariable("id") Long id,
            @RequestBody @Valid UpdateVehicleRequest request) {
        try {
            var vehicleOpt = vehicleRepository.findById(id);

            if (!vehicleOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var vehicle = vehicleOpt.get();
            vehicle.setRegistration(request.registration);
            vehicle.setType(request.type);
            vehicle.setCapacity(request.capacity);
            var vehicleSaved = vehicleRepository.save(vehicle);

            var company = vehicleSaved.getCompany();
            var response = new VehicleDetailedResponse(vehicleSaved.getId(), vehicleSaved.getRegistration(),
                    vehicleSaved.getType(), vehicleSaved.getCapacity(),
                    new CompanyResponse(company.getId(), company.getName()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<HttpStatus> deleteVehicle(@PathVariable("id") Long id) {
        try {
            vehicleRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
