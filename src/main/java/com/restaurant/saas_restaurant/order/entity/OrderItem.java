package com.restaurant.saas_restaurant.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.menu.product.entity.Product;

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
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    // kept for traceability only
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String productName;

    private String variantName;

    // base price + variant modifier at order time
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    // (unitPrice + supplements) * quantity
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemSupplement> supplements = new ArrayList<>();

    public void addSupplement(OrderItemSupplement supplement) {
        supplement.setOrderItem(this);
        supplements.add(supplement);
    }

    public void recalculate() {
        BigDecimal supplementsTotal = supplements.stream()
            .map(OrderItemSupplement::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.lineTotal = unitPrice.add(supplementsTotal).multiply(BigDecimal.valueOf(quantity));
    }
}