package com.universidad_nur.notasnurv3_api.dto;

import java.time.LocalDate;

public record SemesterResponse(
        Integer id,
        Integer number,
        LocalDate startDate,
        LocalDate endDate,
        Integer managementId,
        Integer managementYear
) {
}