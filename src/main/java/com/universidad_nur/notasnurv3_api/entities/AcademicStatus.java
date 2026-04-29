package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum AcademicStatus {
    ACTIVE,
    INACTIVE,
    GRADUATED;

    @JsonCreator
    public static AcademicStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AcademicStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado académico inválido: " + value);
        }
    }
}
