package com.tc.repository;

import com.tc.model.Company;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findCompaniesByCustomersId(Long customerId);
}