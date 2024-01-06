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

import io.swagger.v3.oas.annotations.tags.Tag;

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

    @GetMapping("/companies/{companyId}/drivers")
    public ResponseEntity<List<DriverScopedResponse>> getDriversByCompanyId(@PathVariable("companyId") Long companyId,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Specification<Driver> filters = Specification
                .where(qualification == null ? null : DriverSpecification.hasQualification(qualification));
        Sort sort = sortBy == null ? Sort.unsorted() : Common.sortBy(sortBy);
        var drivers = driverRepository.findAll(filters, PageRequest.of(page, 20, sort));
        var driversResponse = drivers.stream().map(driver -> {
            return new DriverScopedResponse(driver.getId(), driver.getFistName(), driver.getLastName(),
                    driver.getSalary(), driver.getQualifications().stream().map(q -> {
                        return new QualificationResponse(q.getId(), q.getType());
                    }).toList());
        }).toList();
        return new ResponseEntity<>(driversResponse, HttpStatus.OK);
    }

    @PostMapping("/companies/{companyId}/drivers")
    public ResponseEntity<DriverResponse> hireDriver(@PathVariable("companyId") Long companyId,
            @RequestBody CreateDriverRequest request) {
        try {
            var companyOpt = companyRepository.findById(companyId);
            if (!companyOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            var update = new Driver(request.firstName(), request.lastName(), request.salary());
            update.setCompany(companyOpt.get());
            var driver = driverRepository.save(update);

            var response = new DriverResponse(driver.getId(), driver.getFistName(),
                    driver.getLastName(), driver.getSalary());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<DriverDetailedResponse> getDriverById(@PathVariable("id") Long id) {
        try {
            var driverOpt = driverRepository.findById(id);
            if (!driverOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var driver = driverOpt.get();
            var company = driver.getCompany();
            var response = new DriverDetailedResponse(
                    driver.getId(),
                    driver.getFistName(),
                    driver.getLastName(),
                    driver.getSalary(),
                    new CompanyResponse(company.getId(), company.getName()),
                    driver.getQualifications().stream().map(qualification -> {
                        return new QualificationResponse(qualification.getId(), qualification.getType());
                    }).toList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/drivers/{id}")
    public ResponseEntity<DriverDetailedResponse> updateDriver(@PathVariable("id") Long id,
            @RequestBody UpdateDriverRequest request) {
        try {
            var driverOpt = driverRepository.findById(id);
            if (!driverOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var driver = driverOpt.get();
            driver.setFirstName(request.firstName());
            driver.setLastName(request.lastName());
            driver.setSalary(request.salary());
            var updated = driverRepository.save(driver);
            var company = updated.getCompany();
            var response = new DriverDetailedResponse(
                    updated.getId(),
                    updated.getFistName(),
                    updated.getLastName(),
                    updated.getSalary(),
                    new CompanyResponse(company.getId(), company.getName()),
                    driver.getQualifications().stream().map(qualification -> {
                        return new QualificationResponse(qualification.getId(), qualification.getType());
                    }).toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<HttpStatus> deleteDriver(@PathVariable("id") long id) {
        driverRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
