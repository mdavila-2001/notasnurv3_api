package com.universidad_nur.notasnurv3_api.dto;

import java.util.UUID;

public record StudentAbsence(
        UUID enrollmentId,
        UUID studentId,
        String fullName,
        int absencesCount
) {}