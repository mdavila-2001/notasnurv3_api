package com.universidad_nur.notasnurv3_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeResponse(
        UUID id,
        UUID enrollmentId,
        Integer componentId,
        UUID teacherId,
        BigDecimal score
) {}
