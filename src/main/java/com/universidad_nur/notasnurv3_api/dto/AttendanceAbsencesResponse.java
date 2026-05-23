package com.universidad_nur.notasnurv3_api.dto;

import java.util.List;

public record AttendanceAbsencesResponse(
        List<StudentAbsence> students,
        int absenceLimit
) {}