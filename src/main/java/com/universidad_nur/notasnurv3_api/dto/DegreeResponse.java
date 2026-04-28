package com.universidad_nur.notasnurv3_api.dto;

import java.time.LocalDateTime;

public record DegreeResponse(
        Integer id,
        String name,
        String code,
        Integer facultyId,
        String facultyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
