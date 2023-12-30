package com.tc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tc.model.Company;
import com.tc.repository.CompanyRepository;
import com.tc.request.CreateCompanyRequest;
import com.tc.request.UpdateCompanyRequest;
import com.tc.response.CompanyResponse;

@RestController
@RequestMapping("/api")
public class CompanyController {

    @Autowired
    CompanyRepository companyRepository;

    @GetMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable("id") long id) {
       var data = companyRepository.findById(id);

        if (data.isPresent()) {
            var response = new CompanyResponse(
                    data.get().getId(),
                    data.get().getName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
	public ResponseEntity<CompanyResponse> updateCompany(@PathVariable("id") long id, @RequestBody UpdateCompanyRequest request) {
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
	}

    //TODO: Delete company
}