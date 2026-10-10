package com.restaurant.saas_restaurant.zone.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.saas_restaurant.zone.entity.Zone;

public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor<Zone> {
    Optional<Zone> findByIdAndRestaurantIdAndIsDeletedFalse(Long id, Long restaurantId);
    Optional<Zone> findByIdAndIsDeletedTrue(Long id);
    boolean existsByRestaurantIdAndName(Long restaurantId, String name);
    boolean existsByRestaurantIdAndNameAndIdNot(Long restaurantId, String name, Long id);

    @Query(value = "SELECT * FROM zone WHERE restaurant_id = :restaurantId AND is_deleted = true",
           countQuery = "SELECT count(*) FROM zone WHERE restaurant_id = :restaurantId AND is_deleted = true",
           nativeQuery = true)
    Page<Zone> searchDeleted(@Param("restaurantId") Long restaurantId, Pageable pageable);
}