package com.restaurant.saas_restaurant.menu.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.menu.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByRestaurantIdAndIsDeletedFalse(Long restaurantId);
    Optional<Category> findByIdAndRestaurantIdAndIsDeletedFalse(Long id, Long restaurantId);
    Optional<Category> findByIdAndRestaurantIdAndIsDeletedTrue(Long id, Long restaurantId);
}