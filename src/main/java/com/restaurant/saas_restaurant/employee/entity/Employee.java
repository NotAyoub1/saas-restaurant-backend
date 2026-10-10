package com.restaurant.saas_restaurant.employee.entity;

import com.restaurant.saas_restaurant.common.entity.Role.RoleName;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "employee")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends User {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    public boolean hasRole(RoleName name) {
        return this.getRoles().stream().anyMatch(r -> r.getName() == name);
    }
}
