package com.restaurant.saas_restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.restaurant.saas_restaurant.order.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderSummaryResponse {
    private Long id;
    private Long tableId;
    private String tableNumber;
    private String zoneName;
    private Long placedBy;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}