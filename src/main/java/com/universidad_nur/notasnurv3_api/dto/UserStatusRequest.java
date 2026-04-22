package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.NotBlank;

public record UserStatusRequest(
        @NotBlank(message = "El estado es obligatorio")
        String status
) {
}
