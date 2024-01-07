package com.tc.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import com.tc.exception.NotFoundException;
import com.tc.model.Driver;
import com.tc.repository.CompanyRepository;
import com.tc.repository.DriverRepository;
import com.tc.request.CreateDriverRequest;
import com.tc.request.UpdateDriverRequest;
import com.tc.response.CompanyResponse;
import com.tc.response.DriverDetailedResponse;
import com.tc.response.DriverResponse;
import com.tc.response.DriverScopedResponse;
import com.tc.response.QualificationResponse;
import com.tc.specification.Common;
import com.tc.specification.DriverSpecification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Driver")
@RestController
@RequestMapping("/api")
public class DriverController {
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;

    public DriverController(CompanyRepository companyRepository, DriverRepository driverRepository) {
        this.companyRepository = companyRepository;
        this.driverRepository = driverRepository;
    }

    @Operation(summary = "Retrieve drivers of a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The drivers were retrieved"),
            @ApiResponse(responseCode = "404", description = "The company was not found") })
    @GetMapping("/companies/{companyId}/drivers")
    public ResponseEntity<List<DriverScopedResponse>> getDriversByCompanyId(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
            @Parameter(description = "the qualification of the drivers") @RequestParam(required = false) String qualification,
            @Parameter(description = "a set properties to sort by, example value name=ASC") @RequestParam(required = false) String sortBy,
            @Parameter(description = "the requested page") @RequestParam(defaultValue = "0") int page) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));

        Specification<Driver> filters = Specification.where(DriverSpecification.hasCompanyId(company.getId()))
                .and(qualification == null ? null : DriverSpecification.hasQualification(qualification));
        Sort sort = sortBy == null ? Sort.unsorted() : Common.sortBy(sortBy);
        var drivers = driverRepository.findAll(filters, PageRequest.of(page, 20, sort));
        var driversResponse = drivers.stream().map(driver -> {
            return new DriverScopedResponse(driver.getId(), driver.getFirstName(), driver.getLastName(),
                    driver.getSalary(), driver.getQualifications().stream().map(q -> {
                        return new QualificationResponse(q.getId(), q.getType());
                    }).toList());
        }).toList();
        return new ResponseEntity<>(driversResponse, HttpStatus.OK);
    }

    @Operation(summary = "Hire a driver for a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The driver was hired"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404", description = "The company was not found") })
    @PostMapping("/companies/{companyId}/drivers")
    public ResponseEntity<DriverResponse> hireDriver(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
            @Parameter(description = "the create parameters") @RequestBody @Valid CreateDriverRequest request) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));
        var update = new Driver(request.firstName, request.lastName, request.salary);
        update.setCompany(company);
        var driver = driverRepository.save(update);

        var response = new DriverResponse(driver.getId(), driver.getFirstName(),
                driver.getLastName(), driver.getSalary());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve a driver by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The driver was retrieved"),
            @ApiResponse(responseCode = "404", description = "The driver was not found") })
    @GetMapping("/drivers/{id}")
    public ResponseEntity<DriverDetailedResponse> getDriverById(
            @Parameter(description = "the id of the driver") @PathVariable("id") Long id) {
        var driver = driverRepository.findById(id).orElseThrow(() -> new NotFoundException("driver not found"));
        var company = driver.getCompany();
        var response = new DriverDetailedResponse(
                driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getSalary(),
                new CompanyResponse(company.getId(), company.getName()),
                driver.getQualifications().stream().map(qualification -> {
                    return new QualificationResponse(qualification.getId(), qualification.getType());
                }).toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update a driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The driver was updated"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404", description = "The driver was not found") })
    @PutMapping("/drivers/{id}")
    public ResponseEntity<DriverDetailedResponse> updateDriver(
            @Parameter(description = "the id of the driver") @PathVariable("id") Long id,
            @Parameter(description = "the update parameters") @RequestBody @Valid UpdateDriverRequest request) {
        var driver = driverRepository.findById(id).orElseThrow(() -> new NotFoundException("driver not found"));
        driver.setFirstName(request.firstName);
        driver.setLastName(request.lastName);
        driver.setSalary(request.salary);
        var updated = driverRepository.save(driver);
        var company = updated.getCompany();
        var response = new DriverDetailedResponse(
                updated.getId(),
                updated.getFirstName(),
                updated.getLastName(),
                updated.getSalary(),
                new CompanyResponse(company.getId(), company.getName()),
                driver.getQualifications().stream().map(qualification -> {
                    return new QualificationResponse(qualification.getId(), qualification.getType());
                }).toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Delete a driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The driver was deleted or does not exist"), })
    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<HttpStatus> deleteDriver(
            @Parameter(description = "the id of the driver") @PathVariable("id") long id) {
        driverRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
