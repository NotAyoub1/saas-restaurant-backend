package com.restaurant.saas_restaurant.restaurant.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
