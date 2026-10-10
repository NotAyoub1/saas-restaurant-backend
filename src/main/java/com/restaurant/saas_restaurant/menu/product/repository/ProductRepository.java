package com.restaurant.saas_restaurant.menu.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.menu.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByCategoryIdAndIsDeletedFalse(Long categoryId);
    List<Product> findAllByCategoryIdAndIsDeletedTrue(Long categoryId);
    List<Product> findAllByRestaurantIdAndIsDeletedFalse(Long restaurantId);
    Optional<Product> findByIdAndRestaurantIdAndIsDeletedFalse(Long id, Long restaurantId);
}