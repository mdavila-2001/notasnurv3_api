package com.universidad_nur.notasnurv3_api.dto;

import java.math.BigDecimal;

public record ComponentResponse(
        Integer id,
        String name,
        BigDecimal weight,
        String description
) {
}
