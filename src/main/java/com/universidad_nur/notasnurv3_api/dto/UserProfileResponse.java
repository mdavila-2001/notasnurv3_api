package com.universidad_nur.notasnurv3_api.dto;

import com.universidad_nur.notasnurv3_api.entities.UserStatus;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String ci,
        String fullName,
        String email,
        String role,
        UserStatus status
) {
}
