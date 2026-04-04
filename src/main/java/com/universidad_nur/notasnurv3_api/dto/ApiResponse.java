package com.universidad_nur.notasnurv3_api.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {}