package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.NotBlank;

public record FacultyRequest(@NotBlank String name, @NotBlank String code) {}
