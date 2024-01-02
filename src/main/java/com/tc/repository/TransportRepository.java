package com.tc.repository;

import com.tc.model.Transport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRepository<T extends Transport> extends JpaRepository<T, Long> {
    List<T> findByCompanyId(Long companyId);
}