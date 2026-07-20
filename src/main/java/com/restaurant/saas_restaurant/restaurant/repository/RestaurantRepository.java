package com.restaurant.saas_restaurant.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
    Optional<Restaurant> findByEmail(String email);
    boolean existsByEmail(String email);
}
