package com.tc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tc.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findCustomersByCompaniesId(Long companyId);
}