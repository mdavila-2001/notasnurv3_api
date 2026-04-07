package com.universidad_nur.notasnurv3_api.dto;

import java.time.LocalDate;

public record SemesterResponse(
        Long id,
        Integer number,
        LocalDate startDate,
        LocalDate endDate,
        Long managementId,
        Integer managementYear
) {
}