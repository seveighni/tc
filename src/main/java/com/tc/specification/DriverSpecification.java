package com.tc.specification;

import org.springframework.data.jpa.domain.Specification;

import com.tc.model.Driver;
import com.tc.model.Qualification;

import jakarta.persistence.criteria.Join;

public class DriverSpecification {
    public static Specification<Driver> hasQualification(String qualification) {
        return (root, query, cb) -> {
            Join<Driver, Qualification> companyDrivers = root.join("qualifications");
            return cb.equal(companyDrivers.get("type"), qualification);
        };
    }
}
