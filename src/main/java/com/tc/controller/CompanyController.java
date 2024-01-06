package com.tc.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tc.model.Company;
import com.tc.repository.CompanyRepository;
import com.tc.repository.DriverRepository;
import com.tc.request.CreateCompanyRequest;
import com.tc.request.UpdateCompanyRequest;
import com.tc.response.CompanyResponse;
import com.tc.specification.Common;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company")
@RestController
@RequestMapping("/api")
public class CompanyController {
    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository, DriverRepository driverRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/companies")
    public ResponseEntity<List<CompanyResponse>> getCompanies(@RequestParam(required = false) String filterBy,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page) {
        try {

          
            Specification<Company> filter = Common.likeFilter(filterBy);

            Sort sort = sortBy == null ? Sort.unsorted() : Common.sortBy(sortBy);
            var companies = companyRepository.findAll(filter, PageRequest.of(page, 20, sort));
            var response = companies.map(company -> new CompanyResponse(
                    company.getId(),
                    company.getName()));
            return new ResponseEntity<>(response.getContent(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable("id") Long id) {
        try {
            var data = companyRepository.findById(id);

            if (data.isPresent()) {
                var response = new CompanyResponse(
                        data.get().getId(),
                        data.get().getName());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/companies")
    public ResponseEntity<CompanyResponse> createCompany(@RequestBody CreateCompanyRequest request) {
        try {
            var company = companyRepository.save(new Company(request.name()));

            var response = new CompanyResponse(
                    company.getId(),
                    company.getName());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable("id") Long id,
            @RequestBody UpdateCompanyRequest request) {
        try {
            var company = companyRepository.findById(id);

            if (company.isPresent()) {
                var update = company.get();
                update.setName(request.name());
                var updated = companyRepository.save(update);
                var response = new CompanyResponse(
                        updated.getId(),
                        updated.getName());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // TODO: Delete company
}
