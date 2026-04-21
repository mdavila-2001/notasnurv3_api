package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private UUID studentId;

    @NotNull(message = "El ID de la materia es obligatorio")
    private Integer subjectId;
}
