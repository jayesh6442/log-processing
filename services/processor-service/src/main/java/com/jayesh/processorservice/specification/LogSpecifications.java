package com.jayesh.processorservice.specification;

import com.jayesh.processorservice.model.LogEntryEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class LogSpecifications {

    private LogSpecifications() {
    }

    public static Specification<LogEntryEntity> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<LogEntryEntity> hasServiceId(Long serviceId) {
        return (root, query, criteriaBuilder) ->
                serviceId == null ? null : criteriaBuilder.equal(root.get("serviceId"), serviceId);
    }

    public static Specification<LogEntryEntity> hasLevel(String level) {
        return (root, query, criteriaBuilder) ->
                level == null ? null : criteriaBuilder.equal(root.get("level"), level);
    }

    public static Specification<LogEntryEntity> timestampFrom(Instant from) {
        return (root, query, criteriaBuilder) ->
                from == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from);
    }

    public static Specification<LogEntryEntity> timestampTo(Instant to) {
        return (root, query, criteriaBuilder) ->
                to == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to);
    }
}
