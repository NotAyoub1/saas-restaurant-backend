package com.restaurant.saas_restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.restaurant.saas_restaurant.order.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long restaurantId;
    private Long tableId;
    private String tableNumber;
    private Long zoneId;
    private String zoneName;
    private Long placedBy;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}