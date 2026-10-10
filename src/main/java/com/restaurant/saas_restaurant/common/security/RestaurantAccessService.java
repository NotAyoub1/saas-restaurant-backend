package com.restaurant.saas_restaurant.common.security;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.employee.repository.EmployeeRepository;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestaurantAccessService {
    private final CurrentUserService currentUserService;
    private final EmployeeRepository employeeRepository;

    public Set<RoleName> rolesIn(Restaurant restaurant, Long userId) {
        Set<RoleName> roles = EnumSet.noneOf(RoleName.class);
        if (currentUserService.isSuperAdmin(userId)) roles.add(RoleName.SUPER_ADMIN);
        if (restaurant.getOwnerId().equals(userId)) roles.add(RoleName.OWNER);
        employeeRepository.findByIdAndRestaurantIdAndIsDeletedFalse(userId, restaurant.getId())
            .ifPresent(e -> e.getRoles().forEach(r -> roles.add(r.getName())));
        return roles;
    }
}
