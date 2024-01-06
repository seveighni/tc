package com.tc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.tc.model.PassengerTransport;

public interface PassengerTransportRepository
        extends JpaRepository<PassengerTransport, Long>, JpaSpecificationExecutor<PassengerTransport> {
    List<PassengerTransport> findByCompanyId(Long companyId);
}