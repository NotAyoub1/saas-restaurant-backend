package com.restaurant.saas_restaurant.menu.supplement.repository;

import java.util.Collection;
import  java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.menu.supplement.entity.Supplement;

public interface SupplementRepository extends JpaRepository<Supplement, Long> {
    List<Supplement> findAllByProductIdAndIsDeletedFalse(Long productId);
    List<Supplement> findAllByProductIdInAndIsDeletedFalse(Collection<Long> productIds);
    Optional<Supplement> findByIdAndProductIdAndIsDeletedFalse(Long id, Long productId);
}