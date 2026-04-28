package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Modality {
    FACE_TO_FACE("FACE_TO_FACE"),
    BLENDED("BLENDED"),
    ONLINE("ONLINE");

    private final String apiValue;

    Modality(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    @JsonCreator
    public static Modality fromValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "PRESENCIAL", "FACE_TO_FACE" -> FACE_TO_FACE;
            case "SEMIPRESENCIAL", "SEMI_PRESENCIAL", "BLENDED" -> BLENDED;
            case "VIRTUAL", "ONLINE", "EN_LINEA" -> ONLINE;
            default -> throw new IllegalArgumentException("Modalidad inválida: " + value);
        };
    }
}
