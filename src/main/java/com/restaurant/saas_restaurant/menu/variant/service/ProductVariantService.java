package com.restaurant.saas_restaurant.menu.variant.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.menu.product.entity.Product;
import com.restaurant.saas_restaurant.menu.product.repository.ProductRepository;
import com.restaurant.saas_restaurant.menu.variant.dto.ReplaceVariantsRequest;
import com.restaurant.saas_restaurant.menu.variant.dto.VariantItemRequest;
import com.restaurant.saas_restaurant.menu.variant.dto.VariantResponse;
import com.restaurant.saas_restaurant.menu.variant.entity.ProductVariant;
import com.restaurant.saas_restaurant.menu.variant.repository.ProductVariantRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ProductVariantService {
    private final ProductVariantRepository variantRepository;
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
    private VariantResponse toResponse(ProductVariant variant) {
        return new VariantResponse(variant.getId(), variant.getName(), variant.getPriceModifier());
    }
    @Transactional(readOnly = true)
    public List<VariantResponse> list(Long restaurantId, Long productId) {
        findProductByIdAndRestaurantId(productId, restaurantId); // ensures product belongs to restaurant
        return variantRepository.findAllByProductIdAndIsDeletedFalse(productId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    /**
     * Replaces the full set of variants for a product: existing ones are hard-deleted
     * and the incoming list is inserted fresh. Simple and predictable for a small,
     * per-product list — no diffing/matching-by-id needed.
     */
    public List<VariantResponse> replaceAll(Long restaurantId, Long productId, ReplaceVariantsRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Product product = findProductByIdAndRestaurantId(productId, restaurantId);
        List<ProductVariant> existing = variantRepository.findAllByProductIdAndIsDeletedFalse(productId);
        variantRepository.deleteAll(existing);
        List<ProductVariant> newVariants = request.getVariants().stream()
            .map(item -> toEntity(item, product))
            .collect(Collectors.toList());
        return variantRepository.saveAll(newVariants).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    private ProductVariant toEntity(VariantItemRequest item, Product product) {
        ProductVariant variant = new ProductVariant();
        variant.setName(item.getName());
        variant.setPriceModifier(item.getPriceModifier());
        variant.setProduct(product);
        return variant;
    }
}