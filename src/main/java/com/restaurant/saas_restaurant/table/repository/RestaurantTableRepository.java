package com.restaurant.saas_restaurant.table.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.saas_restaurant.table.entity.RestaurantTable;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable,Long> {
    List<RestaurantTable> findAllByZoneId(Long zoneId);
    @Query("""
        SELECT restaurantTable
        FROM RestaurantTable restaurantTable
        WHERE restaurantTable.zone.restaurant.id = :restaurantId
        """)
    List<RestaurantTable> findAllByRestaurantId(@Param("restaurantId") Long restaurantId);
    Optional<RestaurantTable> findByIdAndZoneIdAndZoneRestaurantId(Long tableId,Long zoneId,Long restaurantId);
    Optional<RestaurantTable> findByQrCodeToken(String qrCodeToken);
    boolean existsByZoneIdAndTableNumber(Long zoneId, String tableNumber);
}
