package com.restaurant.saas_restaurant.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndOrderRestaurantId(Long orderId, Long restaurantId);
    boolean existsByOrderId(Long orderId);
}