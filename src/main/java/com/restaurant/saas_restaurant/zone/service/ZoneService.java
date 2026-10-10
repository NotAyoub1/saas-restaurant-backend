package com.restaurant.saas_restaurant.zone.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;
import com.restaurant.saas_restaurant.zone.dto.CreateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.UpdateZoneRequest;
import com.restaurant.saas_restaurant.zone.dto.ZoneResponse;
import com.restaurant.saas_restaurant.zone.entity.Zone;
import com.restaurant.saas_restaurant.zone.repository.ZoneRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    private ZoneResponse toResponse(Zone zone) {
        return new ZoneResponse(
            zone.getId(), 
            zone.getName(), 
            zone.getRestaurant().getId(), 
            zone.getCreated_at(), 
            zone.getUpdated_at()
        );
    }
    private Zone findZoneByIdAndRestaurantId(Long zoneId, Long restaurantId) {
        return zoneRepository.findByIdAndRestaurantIdAndIsDeletedFalse(zoneId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Zone not found: " +zoneId));
    }
    private Restaurant findRestaurantById(Long restaurantId) {
        return restaurantRepository.findByIdAndIsDeletedFalse(restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: "+ restaurantId));
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        boolean isManagerHere = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .map(e -> e.hasRole(RoleName.MANAGER))
            .orElse(false);
        if (isManagerHere) return;
        throw new SecurityException("You are not permitted to manage zones of this restaurant");
    }
    private void assertCanDelete(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        throw new SecurityException("Only Admin or the restaurant OWNER can delete zones");
    }
    public ZoneResponse create(Long restaurantId, CreateZoneRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        if (zoneRepository.existsByRestaurantIdAndName(restaurantId, request.getName()))
            throw new IllegalArgumentException("A zone with that name already exists!");
        Zone zone = new Zone();
        zone.setName(request.getName());
        zone.setRestaurant(restaurant);
        return toResponse(zoneRepository.save(zone));
    }
    @Transactional(readOnly = true)
    public ZoneResponse getById(Long restaurantId, Long zoneId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        return toResponse(findZoneByIdAndRestaurantId(zoneId, restaurantId));
    }
    public ZoneResponse update(Long zoneId, Long restaurantId, UpdateZoneRequest request, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanManage(zone.getRestaurant(), actingUserId);
        if (!zone.getName().equals(request.getName()) && zoneRepository.existsByRestaurantIdAndName(restaurantId, request.getName()))
            throw new IllegalArgumentException("A zone with this name already exists");
        zone.setName(request.getName());
        return toResponse(zoneRepository.save(zone));
    }
    @Transactional(readOnly = true)
    public Page<ZoneResponse> search(Long restaurantId, String name, Long actingUserId, Pageable pageable) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Specification<Zone> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("restaurant").get("id"), restaurantId),
                cb.isFalse(root.get("isDeleted"))
        );
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        return zoneRepository.findAll(spec, pageable).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<ZoneResponse> searchDeleted(Long restaurantId, Long actingUserId, Pageable pageable) {
        if (!currentUserService.isSuperAdmin(actingUserId)) 
            throw new SecurityException("Only Admin can view deleted zones");
        return zoneRepository.searchDeleted(restaurantId, pageable).map(this::toResponse);
    }
    public void softDelete(Long restaurantId, Long zoneId, Long actingUserId) {
        Zone zone = findZoneByIdAndRestaurantId(zoneId, restaurantId);
        assertCanDelete(zone.getRestaurant(), actingUserId);
        zone.markDeleted(String.valueOf(actingUserId));
        zoneRepository.save(zone);
    }
    public void restore(Long restaurantId, Long zoneId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanDelete(restaurant, actingUserId);
        Zone zone = zoneRepository.findByIdAndIsDeletedTrue(zoneId)
            .orElseThrow(() -> new EntityNotFoundException("Zone not found: " + zoneId));
        zone.restore();
        zoneRepository.save(zone);
    }
}