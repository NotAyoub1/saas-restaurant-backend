package com.restaurant.saas_restaurant.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.saas_restaurant.common.entity.BaseEntity;
import com.restaurant.saas_restaurant.restaurant.entity.Restaurant;
import com.restaurant.saas_restaurant.table.entity.RestaurantTable;
import com.restaurant.saas_restaurant.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_order")
@Getter
@Setter
@NoArgsConstructor
public class CustomerOrder extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // null for takeaway orders
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    // the user who placed the order (a customer, or a staff member ordering on their behalf)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placed_by", nullable = false)
    private User placedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}