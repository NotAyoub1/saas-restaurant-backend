package com.restaurant.saas_restaurant.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.saas_restaurant.common.entity.Role;
import com.restaurant.saas_restaurant.common.entity.Role.RoleName;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(RoleName name);
}
