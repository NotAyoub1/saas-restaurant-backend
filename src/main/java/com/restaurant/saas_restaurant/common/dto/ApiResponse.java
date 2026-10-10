package com.restaurant.saas_restaurant.common.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final int status;
    private final String message;
    private final T data;
    private final boolean success;
    private final String timestamp;

    private ApiResponse(int status, String message, T data, boolean success) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = LocalDateTime.now().toString();
    }
    public static <T> ApiResponse<T> success(int Status, String message, T data) {
        return new ApiResponse<T>(Status, message, data, true);
    }
    public static <T> ApiResponse<T> error(int Status, String message) {
        return new ApiResponse<T>(Status, message, null, false);
    }
}
