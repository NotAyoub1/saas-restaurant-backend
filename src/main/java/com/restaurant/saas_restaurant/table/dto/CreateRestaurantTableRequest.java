package com.restaurant.saas_restaurant.table.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRestaurantTableRequest {
    @NotBlank(message = "Table number is required")
    @Size(max = 10, message = "Table number must not exceed 10 characters")
    private String tableNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be atleast 1")
    private Integer capacity;
}
