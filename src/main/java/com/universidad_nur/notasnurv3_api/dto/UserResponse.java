package com.universidad_nur.notasnurv3_api.dto;

import com.universidad_nur.notasnurv3_api.entities.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String ci,
        String name,
        String middleName,
        String lastName,
        String motherLastName,
        String email,
        Role role,
        String status,
        String fullName
) {
}
