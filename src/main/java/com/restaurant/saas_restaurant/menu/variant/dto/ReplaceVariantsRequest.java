package com.restaurant.saas_restaurant.menu.variant.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplaceVariantsRequest {
    @NotNull
    @Valid
    private List<VariantItemRequest> variants;
}