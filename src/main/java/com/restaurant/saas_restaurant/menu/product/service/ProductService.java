package com.restaurant.saas_restaurant.menu.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.security.CurrentUserService;
import com.restaurant.saas_restaurant.common.storage.ImageUploadService;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.menu.category.entity.Category;
import com.restaurant.saas_restaurant.menu.category.repository.CategoryRepository;
import com.restaurant.saas_restaurant.menu.product.dto.CreateProductRequest;
import com.restaurant.saas_restaurant.menu.product.dto.ProductResponse;
import com.restaurant.saas_restaurant.menu.product.dto.UpdateProductRequest;
import com.restaurant.saas_restaurant.menu.product.entity.Product;
import com.restaurant.saas_restaurant.menu.product.repository.ProductRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;
    private final ImageUploadService imageUploadService;

    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: "+id));
    }
    private Product findProductByIdAndRestaurantId(Long productId, Long restaurantId) {
        return productRepository.findByIdAndRestaurantIdAndIsDeletedFalse(productId, restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: "+productId));
    }
    private void assertCanManage(Restaurant restaurant, Long actingUserId) {
        if (currentUserService.isSuperAdmin(actingUserId)) return;
        if (restaurant.getOwnerId().equals(actingUserId)) return;
        boolean isManagerHere = employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(actingUserId, restaurant.getId())
            .map(e -> e.hasRole(RoleName.MANAGER)).orElse(false);
        if (isManagerHere) return;
        throw new SecurityException("You are not permitted to manage the menu of this restaurant");
    }
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getBasePrice(),
            product.getImageUrl(),
            product.getCategory().getId(),
            product.getRestaurant().getId(),
            product.getCreated_at(),
            product.getUpdated_at()
        );
    }
    @Transactional(readOnly = true)
    public List<ProductResponse> listByCategory(Long restaurantId, Long categoryId) {
        return productRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    public ProductResponse create(Long restaurantId, CreateProductRequest request, MultipartFile image, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Category category = categoryRepository.findByIdAndRestaurantIdAndIsDeletedFalse(request.getCategoryId(), restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
        String imageUrl = imageUploadService.upload(image, "products");
        try {
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setBasePrice(request.getBasePrice());
            product.setImageUrl(imageUrl);
            product.setRestaurant(restaurant);
            product.setCategory(category);
            return toResponse(productRepository.save(product));
        } catch (RuntimeException e) {
            imageUploadService.delete(imageUrl);
            throw e;
        }
    }
    public ProductResponse update(Long restaurantId, Long productId, UpdateProductRequest request, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Product product = findProductByIdAndRestaurantId(productId, restaurantId);
        Category category = categoryRepository.findByIdAndRestaurantIdAndIsDeletedFalse(request.getCategoryId(), restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setCategory(category);
        return toResponse(productRepository.save(product));
    }
    public ProductResponse updateImage(Long restaurantId, Long productId, MultipartFile image, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Product product = findProductByIdAndRestaurantId(productId, restaurantId);
        String oldImageUrl = product.getImageUrl();
        String newImageUrl = imageUploadService.upload(image, "products");
        product.setImageUrl(newImageUrl);
        Product saved = productRepository.save(product);
        imageUploadService.delete(oldImageUrl);
        return toResponse(saved);
    }
    public void softDelete(Long restaurantId, Long productId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Product product = findProductByIdAndRestaurantId(productId, restaurantId);
        product.markDeleted(String.valueOf(actingUserId));
        productRepository.save(product);
    }
    public void restore(Long restaurantId, Long productId, Long actingUserId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        assertCanManage(restaurant, actingUserId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        product.restore();
        productRepository.save(product);
    }
}