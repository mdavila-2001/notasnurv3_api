package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    GRADUATED;

    @JsonCreator
    public static UserStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UserStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de usuario inválido: " + value);
        }
    }
}
