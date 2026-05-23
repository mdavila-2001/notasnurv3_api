package com.universidad_nur.notasnurv3_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSettingsResponse {
    private AcademicSettingsDto academic;
    private AttendanceSettingsDto attendance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicSettingsDto {
        private int minPassingGrade;
        private String roundingType;
        private String globalGradesDeadline;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceSettingsDto {
        private int maxAbsencesPresencial;
        private int maxAbsencesSemiPresencial;
    }
}