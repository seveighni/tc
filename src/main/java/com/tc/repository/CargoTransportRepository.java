package com.tc.repository;

import com.tc.model.CargoTransport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CargoTransportRepository
        extends JpaRepository<CargoTransport, Long>, JpaSpecificationExecutor<CargoTransport> {
    List<CargoTransport> findByCompanyId(Long companyId);
}