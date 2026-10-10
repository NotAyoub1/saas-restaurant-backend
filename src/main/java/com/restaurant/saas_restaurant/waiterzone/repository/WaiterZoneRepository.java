package com.restaurant.saas_restaurant.waiterzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.saas_restaurant.waiterzone.entity.WaiterZone;

public interface WaiterZoneRepository extends JpaRepository<WaiterZone, Long> {
    Optional<WaiterZone> findByZoneId(Long zoneId);
    List<WaiterZone> findAllByWaiterId(Long waiterId);

    @Query("""
            SELECT waiterZone
            FROM WaiterZone waiterZone
            WHERE waiterZone.zone.restaurant.id = :restaurantId
            """)
    List<WaiterZone> findAllByRestaurantId(@Param("restaurantId") Long restaurantId);
}