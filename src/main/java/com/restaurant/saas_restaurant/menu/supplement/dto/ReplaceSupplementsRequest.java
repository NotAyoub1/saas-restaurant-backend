package com.restaurant.saas_restaurant.menu.supplement.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplaceSupplementsRequest {
    @NotNull
    @Valid
    private List<SupplementItemRequest> supplements;
}