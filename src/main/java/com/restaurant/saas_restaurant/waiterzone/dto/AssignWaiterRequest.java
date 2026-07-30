package com.restaurant.saas_restaurant.waiterzone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignWaiterRequest {
    @NotNull(message = "Waiter id is required!")
    private Long waiterId;
}
