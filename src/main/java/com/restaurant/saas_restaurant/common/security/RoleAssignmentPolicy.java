package com.restaurant.saas_restaurant.common.security;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;

@Component
public class RoleAssignmentPolicy {
    public void assertCanAssignRoles(RoleName actingUserTopRole, Set<RoleName> rolesToAssign) {
        for (RoleName roles : rolesToAssign) {
            if (roles.rank() >= actingUserTopRole.rank()) {
                throw new SecurityException("You cannot assign a role equal to or higher than your own:"+roles);
            }
        }
    }
}
