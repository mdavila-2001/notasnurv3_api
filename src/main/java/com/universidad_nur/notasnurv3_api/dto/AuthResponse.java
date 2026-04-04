package com.universidad_nur.notasnurv3_api.dto;

public record AuthResponse(
        String token,
        String fullName,
        String role
) {
}
