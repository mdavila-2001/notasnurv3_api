package com.universidad_nur.notasnurv3_api.dto;

public record FacultyStatsResponse(
        Integer facultyId,
        String facultyName,
        long activeStudentsCount
) {}
