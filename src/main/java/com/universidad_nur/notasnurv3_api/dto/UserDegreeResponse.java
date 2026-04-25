package com.universidad_nur.notasnurv3_api.dto;

public record UserDegreeResponse(
        Integer id,
        String studentName,
        String degreeName,
        String type,
        String status
) {}
