package com.restaurant.saas_restaurant.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.restaurant.saas_restaurant.common.entity.Role;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.common.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    @Override
    public void run(String... args) {
        for (RoleName name : RoleName.values()) {
            roleRepository.findByName(name).orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                return roleRepository.save(role);
            });
        }
    }
}
