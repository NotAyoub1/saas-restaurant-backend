package com.restaurant.saas_restaurant.menu.variant.entity;

import java.math.BigDecimal;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.menu.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariant extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceModifier; //added to priceBase

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
