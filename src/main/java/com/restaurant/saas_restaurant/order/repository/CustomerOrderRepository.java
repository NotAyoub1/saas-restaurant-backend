package com.restaurant.saas_restaurant.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.restaurant.saas_restaurant.order.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long>, JpaSpecificationExecutor<CustomerOrder> {
    Optional<CustomerOrder> findByIdAndRestaurantId(Long id, Long restaurantId);
}