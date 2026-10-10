package com.restaurant.saas_restaurant.menu.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.menu.category.dto.CategoryRequest;
import com.restaurant.saas_restaurant.menu.category.dto.CategoryResponse;
import com.restaurant.saas_restaurant.menu.category.entity.Category;
import com.restaurant.saas_restaurant.menu.category.repository.CategoryRepository;
import com.restaurant.saas_restaurant.menu.product.entity.Product;
import com.restaurant.saas_restaurant.menu.product.repository.ProductRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: "+id));
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
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getRestaurant().getId(),
            category.getCreated_at(),
            category.getUpdated_at()
        );
    }
    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long restaurantId) {
        return categoryRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    public CategoryResponse create(Long restaurantId, CategoryRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setRestaurant(restaurant);
        return toResponse(categoryRepository.save(category));
    }
    public CategoryResponse update(Long restaurantId, Long categoryId, CategoryRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Category category = categoryRepository.findByIdAndRestaurantIdAndIsDeletedFalse(categoryId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }
    /**
     * Soft-deletes the category and cascades to all its products
     * (and each product's variants/supplements via ProductService cascade logic).
     */
    public void softDelete(Long restaurantId, Long categoryId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Category category = categoryRepository.findByIdAndRestaurantIdAndIsDeletedFalse(categoryId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        List<Product> products = productRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId);
        for (Product product : products)
            product.markDeleted(String.valueOf(actingUserId));
        productRepository.saveAll(products);
        category.markDeleted(String.valueOf(actingUserId));
        categoryRepository.save(category);
    }
    /**
     * Restores the category and all products that were soft-deleted alongside it.
     * Note: this does NOT restore products that were independently deleted before
     * the category was — only ones deleted in the same cascade sweep would need a
     * deleted_at timestamp comparison if you want that level of precision later.
     */
    public void restore(Long restaurantId, Long categoryId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Category category = categoryRepository.findByIdAndRestaurantIdAndIsDeletedTrue(categoryId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Deleted category not found: " + categoryId));
        category.restore();
        categoryRepository.save(category);
        List<Product> products = productRepository.findAllByCategoryIdAndIsDeletedTrue(categoryId);
        for (Product product : products) 
            product.restore();
        productRepository.saveAll(products);
    }
}