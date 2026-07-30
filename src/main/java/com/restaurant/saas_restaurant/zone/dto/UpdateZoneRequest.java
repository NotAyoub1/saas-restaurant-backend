package com.restaurant.saas_restaurant.zone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateZoneRequest {
    @NotBlank (message = "Zone name is required!")
    @Size (max = 120)
    private String name;
}
