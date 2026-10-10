package com.restaurant.saas_restaurant.waiterzone.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.employee.entity.Employee;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.waiterzone.dto.AssignWaiterRequest;
import com.restaurant.saas_restaurant.waiterzone.dto.WaiterZoneResponse;
import com.restaurant.saas_restaurant.waiterzone.entity.WaiterZone;
import com.restaurant.saas_restaurant.waiterzone.repository.WaiterZoneRepository;
import com.restaurant.saas_restaurant.zone.entity.Zone;
import com.restaurant.saas_restaurant.zone.repository.ZoneRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class WaiterZoneService {
    private final WaiterZoneRepository waiterZoneRepository;
    private final ZoneRepository zoneRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    private WaiterZoneResponse toResponse(WaiterZone waiterZone) {
        return new WaiterZoneResponse(
            waiterZone.getId(),
            waiterZone.getWaiter().getId(),
            waiterZone.getWaiter().getFirstName(),
            waiterZone.getWaiter().getLastName(),
            waiterZone.getZone().getId(),
            waiterZone.getZone().getName(),
            waiterZone.getZone().getRestaurant().getId(),
            waiterZone.getCreated_at(),
            waiterZone.getUpdated_at()
        );
    }
    private Zone findZoneByIdAndRestaurantId(Long zoneId, Long restaurantId) {
        return zoneRepository.findByIdAndRestaurantIdAndIsDeletedFalse(zoneId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Zone not found: " +zoneId));
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        boolean isManagerHere = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .map(e -> e.hasRole(RoleName.MANAGER))
            .orElse(false);
        if (isManagerHere) return;
        throw new SecurityException("You are not permitted to manage waiter assignments for this restaurant");
    }
    public WaiterZoneResponse assign(Long restaurantId, Long zoneId, AssignWaiterRequest request, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        Employee waiter = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(request.getWaiterId(), restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found: "+request.getWaiterId()));
        if (!waiter.hasRole(RoleName.WAITER))
            throw new IllegalArgumentException("Only an employee with role WAITER can be assigned to a zone");
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(zoneId).orElse(new WaiterZone());
        waiterZone.setZone(zone);
        waiterZone.setWaiter(waiter);
        return toResponse(waiterZoneRepository.save(waiterZone));
    }
    @Transactional(readOnly = true)
    public WaiterZoneResponse getByZone(Long restaurantId, Long zoneId, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(zoneId)
            .orElseThrow(() -> new EntityNotFoundException("No waiter is assigned to zone: "+ zoneId));
        return toResponse(waiterZone);
    }
    @Transactional(readOnly = true)
    public List<WaiterZoneResponse> getAllByRestaurant(Long restaurantId, Long actingUserId) {
        // Note: not currently permission-checked at the restaurant level — see message below
        return waiterZoneRepository.findAllByRestaurantId(restaurantId).stream().map(this::toResponse).toList();
    }
    public void remove(Long restaurantId, Long zoneId, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        WaiterZone waiterZone = waiterZoneRepository.findByZoneId(zoneId)
            .orElseThrow(() -> new EntityNotFoundException("No waiter is assigned to zone: " + zoneId));
        waiterZoneRepository.delete(waiterZone);
    }
}