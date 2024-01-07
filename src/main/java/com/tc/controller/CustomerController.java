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

import com.tc.model.Customer;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CustomerRepository;
import com.tc.request.CreateCustomerRequest;
import com.tc.request.UpdateCustomerRequest;
import com.tc.response.CompanyResponse;
import com.tc.response.CustomerDetailedResponse;
import com.tc.response.CustomerResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Customer")
@RestController
@RequestMapping("/api")
public class CustomerController {
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;

    public CustomerController(CompanyRepository companyRepository, CustomerRepository customerRepository) {
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/companies/{companyId}/customers")
    public ResponseEntity<List<CustomerResponse>> getCustomersByCompanyId(@PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var customers = customerRepository.findCustomersByCompaniesId(companyId);
        var customersResponse = customers.stream().map(customer -> {
            return new CustomerResponse(customer.getId(), customer.getName());
        }).toList();
        return new ResponseEntity<>(customersResponse, HttpStatus.OK);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDetailedResponse> getCustomerById(@PathVariable("id") Long id) {
        try {
            var customerOpt = customerRepository.findById(id);

            if (!customerOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var customer = customerOpt.get();
            var companies = customer.getCompanies();
            var response = new CustomerDetailedResponse(customer.getId(), customer.getName(),
                    companies.stream().map(company -> {
                        return new CompanyResponse(company.getId(), company.getName());
                    }).toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/companies/{companyId}/customers")
    public ResponseEntity<CustomerResponse> addCustomerToCompany(@PathVariable("companyId") Long companyId,
            @RequestBody @Valid CreateCustomerRequest request) {
        try {
            var companyOpt = companyRepository.findById(companyId);
            if (!companyOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var company = companyOpt.get();
            var customerId = request.id;
            // customer already exists
            if (customerId != null && customerId != 0) {
                var customerOpt = customerRepository.findById(customerId);
                if (!customerOpt.isPresent()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                var customer = customerOpt.get();
                company.addCustomer(customer);
                companyRepository.save(company);

                var response = new CustomerResponse(customer.getId(), customer.getName());
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            }

            var customer = new Customer(request.name);
            company.addCustomer(customer);
            customerRepository.save(customer);
            var response = new CustomerResponse(customer.getId(), customer.getName());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerDetailedResponse> updateCustomer(@PathVariable("id") Long id,
            @RequestBody @Valid UpdateCustomerRequest request) {
        try {
            var customerOpt = customerRepository.findById(id);

            if (!customerOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var customer = customerOpt.get();
            customer.setName(request.name);
            var updated = customerRepository.save(customer);
            var response = new CustomerDetailedResponse(updated.getId(), updated.getName(),
                    updated.getCompanies().stream().map(company -> {
                        return new CompanyResponse(company.getId(), company.getName());
                    }).toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable("id") Long id) {
        var companies = companyRepository.findCompaniesByCustomersId(id);
        if (!companies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        customerRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/companies/{companyId}/customers/{customerId}")
    public ResponseEntity<HttpStatus> deleteCustomerFromCompany(@PathVariable("companyId") Long companyId,
            @PathVariable("customerId") Long customerId) {
        var companyOpt = companyRepository.findById(companyId);
        if (!companyOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var company = companyOpt.get();

        company.removeCustomer(customerId);
        companyRepository.save(company);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
