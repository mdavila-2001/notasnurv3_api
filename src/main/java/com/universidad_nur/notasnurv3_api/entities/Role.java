package com.universidad_nur.notasnurv3_api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum Role {
    ADMIN("ROLE_ADMIN"),
    TEACHER("ROLE_TEACHER"),
    STUDENT("ROLE_STUDENT");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isTeacher() {
        return this == TEACHER;
    }

    public boolean isStudent() {
        return this == STUDENT;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + value);
        }
    }
}
