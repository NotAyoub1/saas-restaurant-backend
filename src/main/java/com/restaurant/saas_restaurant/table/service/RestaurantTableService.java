package com.restaurant.saas_restaurant.table.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.table.dto.CreateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.dto.RestaurantTableResponse;
import com.restaurant.saas_restaurant.table.dto.UpdateRestaurantTableRequest;
import com.restaurant.saas_restaurant.table.entity.RestaurantTable;
import com.restaurant.saas_restaurant.table.repository.RestaurantTableRepository;
import com.restaurant.saas_restaurant.zone.entity.Zone;
import com.restaurant.saas_restaurant.zone.repository.ZoneRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantTableService {
    private final RestaurantTableRepository restaurantTableRepository;
    private final ZoneRepository zoneRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    private void fillRestaurantTable(RestaurantTable restaurantTable, String tableNumber, Integer capacity) {
        restaurantTable.setTableNumber(tableNumber);
        restaurantTable.setCapacity(capacity);
    }
    private RestaurantTableResponse toResponse(RestaurantTable restaurantTable) {
        return new RestaurantTableResponse(
            restaurantTable.getId(),
            restaurantTable.getTableNumber(),
            restaurantTable.getCapacity(),
            restaurantTable.getQrCodeToken(),
            restaurantTable.getZone().getId(),
            restaurantTable.getZone().getRestaurant().getId(),
            restaurantTable.getCreated_at(),
            restaurantTable.getUpdated_at()
        );
    }
    private RestaurantTable findTableByIdAndZoneAndRestaurant(Long tableId, Long zoneId, Long restaurantId) {
        return restaurantTableRepository.findByIdAndZoneIdAndZoneRestaurantIdAndIsDeletedFalse(tableId, zoneId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Table not found: " + tableId));
    }
    private Zone findZoneByIdAndRestaurantId(Long zoneId, Long restaurantId) {
        return zoneRepository.findByIdAndRestaurantIdAndIsDeletedFalse(zoneId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Zone not found: " + zoneId));
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        boolean isManagerHere = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .map(e -> e.hasRole(RoleName.MANAGER))
            .orElse(false);
        if (isManagerHere) return;
        throw new SecurityException("You are not permitted to manage tables of this restaurant");
    }
    private void assertCanDelete(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        throw new SecurityException("Only Admin or the restaurant OWNER can delete tables");
    }
    // Public — used by the QR-scan flow
    @Transactional(readOnly = true)
    public RestaurantTableResponse getByQrCodeToken(String qrCodeToken) {
        RestaurantTable restaurantTable = restaurantTableRepository.findByQrCodeToken(qrCodeToken)
            .orElseThrow(() -> new EntityNotFoundException("QR code not found: "+ qrCodeToken));
        return toResponse(restaurantTable);
    }
    public RestaurantTableResponse create(CreateRestaurantTableRequest request, Long zoneId, Long restaurantId, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        if (restaurantTableRepository.existsByZoneIdAndTableNumber(zoneId, request.getTableNumber()))
            throw new IllegalArgumentException("A table with this number already exists in this zone");
        RestaurantTable restaurantTable = new RestaurantTable();
        fillRestaurantTable(restaurantTable, request.getTableNumber(), request.getCapacity());
        restaurantTable.setQrCodeToken(UUID.randomUUID().toString());
        restaurantTable.setZone(zone);
        return toResponse(restaurantTableRepository.save(restaurantTable));
    }
    @Transactional(readOnly = true)
    public RestaurantTableResponse getById(Long restaurantId, Long zoneId, Long tableId, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        return toResponse(findTableByIdAndZoneAndRestaurant(tableId, zoneId, restaurantId));
    }
    public RestaurantTableResponse update(UpdateRestaurantTableRequest request, Long zoneId, Long restaurantId, Long tableId, Long actingUserId) {
        RestaurantTable restaurantTable = findTableByIdAndZoneAndRestaurant(tableId, zoneId, restaurantId);
        assertCanManage(restaurantTable.getZone().getRestaurant(), actingUserId);
        if (!restaurantTable.getTableNumber().equals(request.getTableNumber())
                && restaurantTableRepository.existsByZoneIdAndTableNumber(zoneId, request.getTableNumber()))
            throw new IllegalArgumentException("A table with this number already exists in this zone");
        fillRestaurantTable(restaurantTable, request.getTableNumber(), request.getCapacity());
        return toResponse(restaurantTableRepository.save(restaurantTable));
    }
    @Transactional(readOnly = true)
    public Page<RestaurantTableResponse> searchByRestaurant(Long restaurantId, Long actingUserId, Pageable pageable) {
        Specification<RestaurantTable> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("zone").get("restaurant").get("id"), restaurantId),
                cb.isFalse(root.get("isDeleted"))
        );
        return restaurantTableRepository.findAll(spec, pageable).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<RestaurantTableResponse> searchByZone(Long restaurantId, Long zoneId, Long actingUserId, Pageable pageable) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        Specification<RestaurantTable> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("zone").get("id"), zoneId),
                cb.isFalse(root.get("isDeleted"))
        );
        return restaurantTableRepository.findAll(spec, pageable).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<RestaurantTableResponse> searchDeleted(Long restaurantId, Long actingUserId, Pageable pageable) {
        if (!currentUserService.isSuperAdmin(actingUserId))
            throw new SecurityException("Only Admin can view deleted tables");
        return restaurantTableRepository.searchDeleted(restaurantId, pageable).map(this::toResponse);
    }
    public void softDelete(Long tableId, Long zoneId, Long restaurantId, Long actingUserId) {
        RestaurantTable restaurantTable = findTableByIdAndZoneAndRestaurant(tableId, zoneId, restaurantId);
        assertCanDelete(restaurantTable.getZone().getRestaurant(), actingUserId);
        restaurantTable.markDeleted(String.valueOf(actingUserId));
        restaurantTableRepository.save(restaurantTable);
    }
    public void restore(Long tableId, Long zoneId, Long restaurantId, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanDelete(zone.getRestaurant(), actingUserId);
        RestaurantTable restaurantTable = restaurantTableRepository.findByIdAndIsDeletedTrue(tableId)
            .orElseThrow(() -> new EntityNotFoundException("Table not found: "+tableId));
        restaurantTable.restore();
        restaurantTableRepository.save(restaurantTable);
    }
}