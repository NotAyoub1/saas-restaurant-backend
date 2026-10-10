package com.restaurant.saas_restaurant.menu.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.saas_restaurant.menu.category.entity.Category;
import com.restaurant.saas_restaurant.menu.category.repository.CategoryRepository;
import com.restaurant.saas_restaurant.menu.dto.MenuCategoryResponse;
import com.restaurant.saas_restaurant.menu.dto.MenuProductResponse;
import com.restaurant.saas_restaurant.menu.dto.MenuResponse;
import com.restaurant.saas_restaurant.menu.product.entity.Product;
import com.restaurant.saas_restaurant.menu.product.repository.ProductRepository;
import com.restaurant.saas_restaurant.menu.supplement.dto.SupplementResponse;
import com.restaurant.saas_restaurant.menu.supplement.repository.SupplementRepository;
import com.restaurant.saas_restaurant.menu.variant.dto.VariantResponse;
import com.restaurant.saas_restaurant.menu.variant.repository.ProductVariantRepository;
import com.restaurant.saas_restaurant.restaurant.repository.RestaurantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final SupplementRepository supplementRepository;

    public MenuResponse getMenu(Long restaurantId) {
        restaurantRepository.findByIdAndIsDeletedFalse(restaurantId)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));
            
        List<Category> categories = categoryRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId);
        List<Product> products = productRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId);
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        Map<Long, List<VariantResponse>> variantsByProduct = productIds.isEmpty() ? Map.of() :
            variantRepository.findAllByProductIdInAndIsDeletedFalse(productIds).stream()
                .collect(Collectors.groupingBy(
                    v -> v.getProduct().getId(),
                    Collectors.mapping(v -> new VariantResponse(v.getId(), v.getName(), v.getPriceModifier()),
                        Collectors.toList())));

        Map<Long, List<SupplementResponse>> supplementsByProduct = productIds.isEmpty() ? Map.of() :
            supplementRepository.findAllByProductIdInAndIsDeletedFalse(productIds).stream()
                .collect(Collectors.groupingBy(
                    s -> s.getProduct().getId(),
                    Collectors.mapping(s -> new SupplementResponse(s.getId(), s.getName(), s.getPrice()),
                        Collectors.toList())));

        Map<Long, List<MenuProductResponse>> productsByCategory = products.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCategory().getId(),
                Collectors.mapping(p -> new MenuProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getBasePrice(),
                        p.getImageUrl(),
                        variantsByProduct.getOrDefault(p.getId(), List.of()),
                        supplementsByProduct.getOrDefault(p.getId(), List.of())),
                    Collectors.toList())));

        List<MenuCategoryResponse> categoryResponses = categories.stream()
            .map(c -> new MenuCategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                productsByCategory.getOrDefault(c.getId(), List.of())))
            .collect(Collectors.toList());

        return new MenuResponse(restaurantId, categoryResponses);
    }
}