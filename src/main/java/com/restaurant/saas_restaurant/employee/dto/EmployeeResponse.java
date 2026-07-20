package com.restaurant.saas_restaurant.employee.dto;

import java.time.LocalDateTime;

import com.restaurant.saas_restaurant.employee.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
