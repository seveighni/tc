package com.tc.repository;

import com.tc.model.Driver;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByCompanyId(Long companyId);
}