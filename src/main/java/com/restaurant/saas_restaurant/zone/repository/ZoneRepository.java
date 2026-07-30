package com.restaurant.saas_restaurant.zone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.zone.entity.Zone;

public interface ZoneRepository extends JpaRepository<Zone,Long> {
    List<Zone> getAllZonesByRestaurantId(Long restaurantId);
    Optional<Zone> getZoneByRestaurantId(Long zoneId, Long restaurantId);
    Optional<Zone> findByIdAndRestaurantId(Long id, Long restaurantId);
    boolean existsByRestaurantIdAndName(Long restaurantId, String name);
}
