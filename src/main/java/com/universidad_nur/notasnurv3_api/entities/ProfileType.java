package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum ProfileType {
    STUDENT,
    TEACHER;

    @JsonCreator
    public static ProfileType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ProfileType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de perfil inválido: " + value);
        }
    }
}
