package com.restaurant.saas_restaurant.zone.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ZoneResponse {
    private Long id;
    private String name;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; 
}
