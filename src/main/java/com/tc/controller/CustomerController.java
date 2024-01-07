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

import com.tc.exception.BadRequestException;
import com.tc.exception.NotFoundException;
import com.tc.model.Customer;
import com.tc.repository.CompanyRepository;
import com.tc.repository.CustomerRepository;
import com.tc.request.CreateCustomerRequest;
import com.tc.request.UpdateCustomerRequest;
import com.tc.response.CompanyResponse;
import com.tc.response.CustomerDetailedResponse;
import com.tc.response.CustomerResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Retrieve customers of a company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The customers were retrieved"),
            @ApiResponse(responseCode = "404", description = "The company was not found"), })
    @GetMapping("/companies/{companyId}/customers")
    public ResponseEntity<List<CustomerResponse>> getCustomersByCompanyId(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));
        var customers = customerRepository.findCustomersByCompaniesId(company.getId());
        var customersResponse = customers.stream().map(customer -> {
            return new CustomerResponse(customer.getId(), customer.getName());
        }).toList();
        return new ResponseEntity<>(customersResponse, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve a customer by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The customer was retrieved"),
            @ApiResponse(responseCode = "404", description = "The customer was not found"), })
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDetailedResponse> getCustomerById(
            @Parameter(description = "the id of the customer") @PathVariable("id") Long id) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("customer not found"));
        var companies = customer.getCompanies();
        var response = new CustomerDetailedResponse(customer.getId(), customer.getName(),
                companies.stream().map(company -> {
                    return new CompanyResponse(company.getId(), company.getName());
                }).toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Create a customer for the company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The customer was created"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404") })
    @PostMapping("/companies/{companyId}/customers")
    public ResponseEntity<CustomerResponse> addCustomerToCompany(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
            @Parameter(description = "the create parameters") @RequestBody @Valid CreateCustomerRequest request) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));
        var customerId = request.id;
        // customer already exists
        if (customerId != null && customerId != 0) {
            var customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new NotFoundException("customer not found"));
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
    }

    @Operation(summary = "Update a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The customer was updated"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404", description = "The customer was not found") })
    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerDetailedResponse> updateCustomer(
            @Parameter(description = "the id of the customer") @PathVariable("id") Long id,
            @Parameter(description = "the update parameters") @RequestBody @Valid UpdateCustomerRequest request) {
        var customer = customerRepository.findById(id).orElseThrow(() -> new NotFoundException("customer not found"));
        customer.setName(request.name);
        var updated = customerRepository.save(customer);
        var response = new CustomerDetailedResponse(updated.getId(), updated.getName(),
                updated.getCompanies().stream().map(company -> {
                    return new CompanyResponse(company.getId(), company.getName());
                }).toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Delete a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The was deleted or does not exist"),
            @ApiResponse(responseCode = "400", description = "The customer is is still associated with companies") })
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<HttpStatus> deleteCustomer(
            @Parameter(description = "the id of the customer") @PathVariable("id") Long id) {
        var companies = companyRepository.findCompaniesByCustomersId(id);
        if (!companies.isEmpty()) {
            throw new BadRequestException("customer is still associated with companies");
        }
        customerRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Remove a customer from company customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The customer was removed from company customers"),
            @ApiResponse(responseCode = "404", description = "The company was not found") })
    @DeleteMapping("/companies/{companyId}/customers/{customerId}")
    public ResponseEntity<HttpStatus> deleteCustomerFromCompany(
            @Parameter(description = "the id of the company") @PathVariable("companyId") Long companyId,
            @Parameter(description = "the id of the company") @PathVariable("customerId") Long customerId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company not found"));
        company.removeCustomer(customerId);
        companyRepository.save(company);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
