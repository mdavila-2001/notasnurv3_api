package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AttendanceBulkRequest(
        @NotNull(message = "El ID de la materia es requerido")
        Integer subjectId,

        @NotNull(message = "La fecha es requerida")
        LocalDate date,

        @NotEmpty(message = "Debe enviar al menos un registro de asistencia")
        @Valid
        List<StudentAttendance> records
) {}
