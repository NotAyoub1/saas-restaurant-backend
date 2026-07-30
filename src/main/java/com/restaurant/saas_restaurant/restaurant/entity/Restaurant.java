package com.restaurant.saas_restaurant.restaurant.entity;

import java.util.HashSet;
import java.util.Set;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.restaurant.enums.PaymentMethod;
import com.restaurant.saas_restaurant.restaurant.enums.ServiceType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
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

    @ElementCollection
    @CollectionTable (name = "restaurant_payment_methods", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private Set<PaymentMethod> acceptedPaymentMethods = new HashSet<>();

    @ElementCollection
    @CollectionTable (name = "restaurant_service_types", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private Set<ServiceType> serviceTypes = new HashSet<>();
}
