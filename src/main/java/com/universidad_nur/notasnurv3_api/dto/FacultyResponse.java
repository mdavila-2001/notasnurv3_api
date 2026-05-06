package com.universidad_nur.notasnurv3_api.dto;

import java.time.LocalDateTime;

public record FacultyResponse(
        Integer id,
        String name,
        String code,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
