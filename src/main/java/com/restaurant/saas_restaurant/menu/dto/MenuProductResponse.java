package com.restaurant.saas_restaurant.menu.dto;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.saas_restaurant.menu.supplement.dto.SupplementResponse;
import com.restaurant.saas_restaurant.menu.variant.dto.VariantResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private List<VariantResponse> variants;
    private List<SupplementResponse> supplements;
}