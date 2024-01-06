package com.tc.repository;

import com.tc.model.Driver;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {
    List<Driver> findByCompanyId(Long companyId);
}