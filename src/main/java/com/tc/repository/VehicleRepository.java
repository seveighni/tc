package com.tc.repository;

import com.tc.model.Vehicle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCompanyId(Long companyId);
}