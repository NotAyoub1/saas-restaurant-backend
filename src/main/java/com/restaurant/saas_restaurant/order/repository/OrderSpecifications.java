package com.restaurant.saas_restaurant.order.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.restaurant.saas_restaurant.order.entity.CustomerOrder;
import com.restaurant.saas_restaurant.order.entity.OrderStatus;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<CustomerOrder> filter(Long restaurantId, Long zoneId, Long tableId,
            OrderStatus status, LocalDate from, LocalDate to, Long placedById) {

        Specification<CustomerOrder> spec =
            (root, query, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId);

        if (zoneId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("table").get("zone").get("id"), zoneId));
        if (tableId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("table").get("id"), tableId));
        if (status != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (placedById != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("placedBy").get("id"), placedById));
        if (from != null)
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.<LocalDateTime>get("created_at"), from.atStartOfDay()));
        if (to != null)
            spec = spec.and((root, query, cb) ->
                cb.lessThan(root.<LocalDateTime>get("created_at"), to.plusDays(1).atStartOfDay()));

        return spec;
    }
}