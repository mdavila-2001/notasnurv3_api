package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DegreeRequest(@NotBlank String name, @NotBlank String code, @NotNull Integer facultyId) {}
