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
import com.tc.model.Employee;
import com.tc.repository.CompanyRepository;
import com.tc.repository.EmployeeRepository;
import com.tc.request.CreateEmployeeRequest;
import com.tc.request.UpdateEmployeeRequest;
import com.tc.response.EmployeeDetailedResponse;
import com.tc.response.EmployeeResponse;

@RestController
@RequestMapping("/api")
public class EmployeeController {
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(CompanyRepository companyRepository, EmployeeRepository employeeRepository) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/companies/{companyId}/employees")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByCompanyId(@PathVariable("companyId") Long companyId) {
        var company = companyRepository.findById(companyId);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Employee> employees = employeeRepository.findByCompanyId(companyId);
        var employeesResponse = employees.stream().map(employee -> {
            return new EmployeeResponse(employee.getId(), employee.getFistName(), employee.getLastName());
        }).toList();
        return new ResponseEntity<>(employeesResponse, HttpStatus.OK);
    }

    @PostMapping("/companies/{companyId}/employees")
    public ResponseEntity<EmployeeResponse> hireEmployee(@PathVariable("companyId") Long companyId,
            @RequestBody CreateEmployeeRequest request) {
        try {
            var employeeOpt = companyRepository.findById(companyId).map(company -> {
                var update = new Employee(request.firstName(), request.lastName());
                update.setCompany(company);
                return employeeRepository.save(update);
            });
            if (!employeeOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var employee = employeeOpt.get();
            var response = new EmployeeResponse(employee.getId(), employee.getFistName(),
                    employee.getLastName());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeDetailedResponse> getEmployeeById(@PathVariable("id") Long id) {
        try {
            var employeeOpt = employeeRepository.findById(id);
            if (!employeeOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var employee = employeeOpt.get();
            var response = new EmployeeDetailedResponse(
                    employee.getId(),
                    employee.getFistName(),
                    employee.getLastName(),
                    employee.getCompany().getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<EmployeeDetailedResponse> updateEmployee(@PathVariable("id") Long id,
            @RequestBody UpdateEmployeeRequest request) {
        try {
            var employeeOpt = employeeRepository.findById(id);
            if (!employeeOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var employee = employeeOpt.get();
            employee.setFirstName(request.firstName());
            employee.setLastName(request.lastName());
            var updated = employeeRepository.save(employee);
            var response = new EmployeeDetailedResponse(
                    updated.getId(),
                    updated.getFistName(),
                    updated.getLastName(),
                    updated.getCompany().getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable("id") long id) {
        employeeRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
