package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum EnrollmentStatus {
    ACTIVE,
    WITHDRAWN;

    @JsonCreator
    public static EnrollmentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EnrollmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de inscripción inválido: " + value);
        }
    }
}
