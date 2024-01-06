package com.tc.specification;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class Common {
    public static Sort sortBy(String input) {
        if (input == null) {
            return null;
        }
        Sort s = null;
        var fields = input.split(",");
        for (var f : fields) {
            var split = f.split("=");
            var prop = split[0];
            var val = split[1];
            if (val.equals("asc")) {
                if (s == null) {
                    s = Sort.by(Sort.Direction.ASC, prop);
                } else {
                    s = s.and(Sort.by(Sort.Direction.ASC, prop));
                }
            } else if (val.equals("desc")) {
                if (s == null) {
                    s = Sort.by(Sort.Direction.DESC, prop);
                } else {
                    s = s.and(Sort.by(Sort.Direction.DESC, prop));
                }
            }
        }
        return s;
    }

    public static <T> Specification<T> likeFilter(String input) {
        if (input == null) {
            return null;
        }
        Specification<T> filter = Specification.where((root, query, cb) -> {
            Predicate predicate = null;
            var filters = input.split(",");
            for (var f : filters) {
                var split = f.split("=");
                var prop = split[0];
                var val = split[1];
                predicate = cb.like(cb.upper(root.get(prop)), "%" + val.toUpperCase() + "%");
            }

            return predicate;
        });

        return filter;
    }
}
