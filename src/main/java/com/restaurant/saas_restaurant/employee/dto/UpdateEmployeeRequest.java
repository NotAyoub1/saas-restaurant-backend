package com.restaurant.saas_restaurant.employee.dto;

import java.util.Set;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeRequest {
    @NotEmpty(message = "At least one role is required")
    private Set<RoleName> roles;
}