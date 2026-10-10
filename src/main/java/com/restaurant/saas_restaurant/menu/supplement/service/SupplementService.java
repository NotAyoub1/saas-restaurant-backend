package com.restaurant.saas_restaurant.menu.supplement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.menu.product.entity.Product;
import com.restaurant.saas_restaurant.menu.product.repository.ProductRepository;
import com.restaurant.saas_restaurant.menu.supplement.dto.ReplaceSupplementsRequest;
import com.restaurant.saas_restaurant.menu.supplement.dto.SupplementItemRequest;
import com.restaurant.saas_restaurant.menu.supplement.dto.SupplementResponse;
import com.restaurant.saas_restaurant.menu.supplement.entity.Supplement;
import com.restaurant.saas_restaurant.menu.supplement.repository.SupplementRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class SupplementService {
    private final SupplementRepository supplementRepository;
    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + id));
    }
    private Product findProductByIdAndRestaurantId(Long productId, Long restaurantId) {
        return productRepository.findByIdAndRestaurantIdAndIsDeletedFalse(productId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        boolean isManagerHere = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .map(e -> e.hasRole(RoleName.MANAGER))
            .orElse(false);
        if (isManagerHere) return;
        throw new SecurityException("You are not permitted to manage the menu of this restaurant");
    }
    private SupplementResponse toResponse(Supplement supplement) {
        return new SupplementResponse(supplement.getId(), supplement.getName(), supplement.getPrice());
    }
    @Transactional(readOnly = true)
    public List<SupplementResponse> list(Long restaurantId, Long productId) {
        findProductByIdAndRestaurantId(productId, restaurantId);
        return supplementRepository.findAllByProductIdAndIsDeletedFalse(productId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    public List<SupplementResponse> replaceAll(Long restaurantId, Long productId, ReplaceSupplementsRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Product product = findProductByIdAndRestaurantId(productId, restaurantId);
        List<Supplement> existing = supplementRepository.findAllByProductIdAndIsDeletedFalse(productId);
        supplementRepository.deleteAll(existing);

        List<Supplement> newSupplements = request.getSupplements().stream()
            .map(item -> toEntity(item, product))
            .collect(Collectors.toList());
        return supplementRepository.saveAll(newSupplements).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    private Supplement toEntity(SupplementItemRequest item, Product product) {
        Supplement supplement = new Supplement();
        supplement.setName(item.getName());
        supplement.setPrice(item.getPrice());
        supplement.setProduct(product);
        return supplement;
    }
}