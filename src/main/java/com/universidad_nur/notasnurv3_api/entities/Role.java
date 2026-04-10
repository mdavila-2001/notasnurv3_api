package com.universidad_nur.notasnurv3_api.entities;

public enum Role {
    ADMIN,
    TEACHER,
    STUDENT;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isTeacher() {
        return this == TEACHER;
    }

    public boolean isStudent() {
        return this == STUDENT;
    }

    public String authority() {
        return switch (this) {
            case ADMIN -> "ROLE_ADMIN";
            case TEACHER -> "ROLE_TEACHER";
            case STUDENT -> "ROLE_STUDENT";
        };
    }
}
