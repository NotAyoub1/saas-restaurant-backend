package com.restaurant.saas_restaurant.menu.variant.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.menu.variant.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findAllByProductIdAndIsDeletedFalse(Long productId);
    List<ProductVariant> findAllByProductIdInAndIsDeletedFalse(Collection<Long> productIds);
    Optional<ProductVariant> findByIdAndProductIdAndIsDeletedFalse(Long id, Long productId);
}