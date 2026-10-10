package com.restaurant.saas_restaurant.table.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurant.saas_restaurant.table.entity.RestaurantTable;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long>, JpaSpecificationExecutor<RestaurantTable> {

    Optional<RestaurantTable> findByIdAndZoneIdAndZoneRestaurantIdAndIsDeletedFalse(Long tableId, Long zoneId, Long restaurantId);
    Optional<RestaurantTable> findByIdAndIsDeletedTrue(Long id);
    Optional<RestaurantTable> findByQrCodeToken(String qrCodeToken);
    boolean existsByZoneIdAndTableNumber(Long zoneId, String tableNumber);
    boolean existsByZoneIdAndTableNumberAndIdNot(Long zoneId, String tableNumber, Long id);

    @Query(value = "SELECT rt.* FROM restaurant_table rt " +
                   "JOIN zone z ON rt.zone_id = z.id " +
                   "WHERE z.restaurant_id = :restaurantId AND rt.is_deleted = true",
           countQuery = "SELECT count(*) FROM restaurant_table rt " +
                        "JOIN zone z ON rt.zone_id = z.id " +
                        "WHERE z.restaurant_id = :restaurantId AND rt.is_deleted = true",
           nativeQuery = true)
    Page<RestaurantTable> searchDeleted(@Param("restaurantId") Long restaurantId, Pageable pageable);
}