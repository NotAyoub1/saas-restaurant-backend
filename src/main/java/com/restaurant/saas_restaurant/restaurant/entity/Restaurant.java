package com.restaurant.saas_restaurant.restaurant.entity;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity //pour cree une entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table (name = "restaurant")

public class Restaurant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;
}
