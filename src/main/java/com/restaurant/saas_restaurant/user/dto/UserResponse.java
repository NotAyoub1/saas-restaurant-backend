package com.restaurant.saas_restaurant.user.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<RoleName> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}