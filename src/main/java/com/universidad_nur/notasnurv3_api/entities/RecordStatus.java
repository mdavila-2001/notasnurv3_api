package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum RecordStatus {
    DRAFT,
    PUBLISHED,
    ACTIVE,
    INACTIVE,
    CLOSED;

    @JsonCreator
    public static RecordStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RecordStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de registro inválido: " + value);
        }
    }
}
