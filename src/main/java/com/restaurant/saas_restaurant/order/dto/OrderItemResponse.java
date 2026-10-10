package com.restaurant.saas_restaurant.order.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String variantName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private List<OrderSupplementResponse> supplements;
}