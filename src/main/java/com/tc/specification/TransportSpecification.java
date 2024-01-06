package com.tc.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Path;

public class TransportSpecification {
    public static <T> Specification<T> hasDestination(String destination) {
        return (root, query, cb) -> {
            return cb.equal(root.get("endAddress"), destination);
        };
    }

    public static <T> Specification<T> hasCompanyId(Long companyId) {
        return (root, query, cb) -> {
            Path<T> transport = root.get("company");
            return transport.get("id").in(companyId);
        };
    }

    public static <T> Specification<T> endDateInRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            return cb.between(root.get("endDate"), startDate, endDate);
        };
    }
}
