package com.restaurant.saas_restaurant.order.entity;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    IN_PREPARATION,
    READY,
    SERVED,
    PAID,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        if (this == CANCELLED) return false;
        if (target == CANCELLED) return this != PAID;
        return switch (this) {
            case CREATED -> target == CONFIRMED;
            case CONFIRMED -> target == IN_PREPARATION;
            case IN_PREPARATION -> target == READY;
            case READY -> target == SERVED;
            case SERVED -> target == PAID;
            default -> false;
        };
    }
}
