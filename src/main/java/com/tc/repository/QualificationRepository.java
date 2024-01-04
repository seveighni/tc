package com.tc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tc.model.Qualification;

public interface QualificationRepository extends JpaRepository<Qualification, Long> {
    List<Qualification> findQualificationsByDriversId(Long driverId);
}