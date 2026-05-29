package vn.hoidanit.springrestwithai.feature.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.PredicateSpecification;

import vn.hoidanit.springrestwithai.feature.user.dto.UserFilterRequest;

public class UserSpecification {

    public static PredicateSpecification<User> build(UserFilterRequest filter) {
        return (from, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("name")),
                                "%" + filter.name().toLowerCase() + "%"));
            }

            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("email")),
                                "%" + filter.email().toLowerCase() + "%"));
            }

            if (filter.address() != null && !filter.address().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("address")),
                                "%" + filter.address().toLowerCase() + "%"));
            }

            if (filter.ageFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(from.get("age"), filter.ageFrom()));
            }

            if (filter.ageTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(from.get("age"), filter.ageTo()));
            }

            if (filter.gender() != null) {
                predicates.add(
                        cb.equal(from.get("gender"), filter.gender()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
