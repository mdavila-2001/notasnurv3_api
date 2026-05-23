package com.universidad_nur.notasnurv3_api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordResponse(
        UUID enrollmentId,
        UUID studentId,
        String fullName,
        String status,
        LocalDate date
) {}