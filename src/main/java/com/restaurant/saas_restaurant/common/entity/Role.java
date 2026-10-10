package com.restaurant.saas_restaurant.common.entity;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    public enum RoleName {
        SUPER_ADMIN,
        OWNER,
        MANAGER,
        COOK,
        WAITER,
        CASHIER,
        USER;

        private static final Map<RoleName,Integer> RANK = Map.of(
            SUPER_ADMIN, 100,
            OWNER, 80,
            MANAGER, 60,
            COOK, 20,
            WAITER, 20,
            CASHIER, 20,
            USER, 0
        );
        public int rank() {
            return RANK.get(this);
        }
    }
}
