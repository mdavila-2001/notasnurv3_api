package com.universidad_nur.notasnurv3_api.dto;

import com.universidad_nur.notasnurv3_api.entities.ProfileType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserDegreeRequest(
        @NotNull UUID userId,
        @NotNull Integer degreeId,
        @NotNull ProfileType type
) {}
