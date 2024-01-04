package com.tc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tc.model.PassengerTransport;

public interface PassengerTransportRepository extends JpaRepository<PassengerTransport, Long> {
    List<PassengerTransport> findByCompanyId(Long companyId);
}