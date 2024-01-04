package com.tc.repository;

import com.tc.model.CargoTransport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CargoTransportRepository extends JpaRepository<CargoTransport, Long> {
    List<CargoTransport> findByCompanyId(Long companyId);
}