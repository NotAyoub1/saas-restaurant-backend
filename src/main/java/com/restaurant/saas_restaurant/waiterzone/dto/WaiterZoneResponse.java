package com.restaurant.saas_restaurant.waiterzone.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaiterZoneResponse {
    private Long id;
    private Long waiterId;
    private String waiterFirstName;
    private String waiterLastName;
    private Long zoneId;
    private String zoneName;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
