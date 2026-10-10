package com.restaurant.saas_restaurant.restaurant.service;

import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.restaurant.dto.CreateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.dto.RestaurantResponse;
import com.restaurant.saas_restaurant.restaurant.dto.UpdateRestaurantRequest;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserService currentUserService;

    private void applyRequest(Restaurant restaurant, CreateRestaurantRequest request) {
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setAcceptedPaymentMethods(new HashSet<>(request.getAcceptedPaymentMethods()));
        restaurant.setServiceTypes(new HashSet<>(request.getServiceTypes()));
    }
    private void applyRequest(Restaurant restaurant, UpdateRestaurantRequest request) {
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setAcceptedPaymentMethods(new HashSet<>(request.getAcceptedPaymentMethods()));
        restaurant.setServiceTypes(new HashSet<>(request.getServiceTypes()));
    }
    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
            restaurant.getId(), 
            restaurant.getName(), 
            restaurant.getAddress(), 
            restaurant.getPhone(), 
            restaurant.getEmail(), 
            restaurant.getCreated_at(), 
            restaurant.getUpdated_at(), 
            new HashSet<>(restaurant.getAcceptedPaymentMethods()), 
            new HashSet<>(restaurant.getServiceTypes())
        );
    }
    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: "+id));
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        throw new SecurityException("You are not permitted to manage this restaurant");
    }
    private void assertCanDelete(Restaurant restaurant, Long actingUserId) {
        assertCanManage(restaurant, actingUserId);
    }
    public RestaurantResponse create(CreateRestaurantRequest request, Long ownerId) {
        if (restaurantRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("A restaurant already uses this email");
        Restaurant restaurant = new Restaurant();
        applyRequest(restaurant, request);
        restaurant.setOwnerId(ownerId);
        return toResponse(restaurantRepository.save(restaurant));
    }
    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        return toResponse(restaurant);
    }
    public RestaurantResponse update(Long id ,UpdateRestaurantRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(id);
        assertCanManage(restaurant, actingUserId);
        if (restaurantRepository.existsByEmailAndIdNot(request.getEmail(),id))
            throw new IllegalArgumentException("A restaurant already uses this email");
        applyRequest(restaurant, request);
        return toResponse(restaurantRepository.save(restaurant));
    }
    public void softDelete(Long id, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(id);
        assertCanDelete(restaurant, actingUserId);
        restaurant.markDeleted(String.valueOf(actingUserId));
        restaurantRepository.save(restaurant);
    }
    public void restore(Long id, Long actingUserId) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsDeletedTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: "+id));
        assertCanDelete(restaurant, actingUserId);
        restaurant.restore();
        restaurantRepository.save(restaurant);
    }
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> search(String name, String email, Long ownerId, Pageable pageable) {
        Specification<Restaurant> spec = (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (ownerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ownerId"), ownerId));
        }
        return restaurantRepository.findAll(spec, pageable).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> searchDeleted(Long actingUserId, Pageable pageable) {
        if (!currentUserService.isSuperAdmin(actingUserId)) {
            throw new SecurityException("Only Admin can view deleted restaurants");
        }
        return restaurantRepository.searchDeleted(pageable).map(this::toResponse);
    }
}