package com.restaurant.saas_restaurant.restaurant.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant,Long>, JpaSpecificationExecutor<Restaurant> {
    Optional<Restaurant> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id); //fixes the update email bug

    Optional<Restaurant> findByIdAndIsDeletedFalse(Long id);
    Optional<Restaurant> findByIdAndIsDeletedTrue(Long id);

    @Query(value = "SELECT * FROM restaurant WHERE is_deleted = true",
        countQuery = "SELECT count(*) FROM restaurant WHERE is_deleted = true",
        nativeQuery = true)
    Page<Restaurant> searchDeleted(Pageable pageable);
}
