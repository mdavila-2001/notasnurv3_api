package com.universidad_nur.notasnurv3_api.dto;

import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StudentAttendance(
        @NotNull(message = "El ID de la inscripción es requerido")
        UUID enrollmentId,

        @NotNull(message = "El estado de la asistencia es requerido")
        AttendanceStatus status
) {}
