package com.universidad_nur.notasnurv3_api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record GradeBulkRequest(
                @NotEmpty(message = "Debes enviar al menos una nota") List<@Valid GradeRequest> grades) {
}