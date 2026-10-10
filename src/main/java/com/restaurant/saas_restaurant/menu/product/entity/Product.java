package com.restaurant.saas_restaurant.menu.product.entity;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.menu.category.entity.Category;
import com.restaurant.saas_restaurant.menu.supplement.entity.Supplement;
import com.restaurant.saas_restaurant.menu.variant.entity.ProductVariant;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProductVariant> variants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Supplement> supplements = new LinkedHashSet<>();
}
