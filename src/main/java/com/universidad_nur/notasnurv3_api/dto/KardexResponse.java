package com.universidad_nur.notasnurv3_api.dto;

import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KardexResponse {
    private String studentName;
    private String degreeName;
    private Map<String, List<SubjectGradeResponse>> historyBySemester;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubjectGradeResponse {
        private String subjectCode;
        private String subjectName;
        private Integer finalScore;
        private EnrollmentStatus status;
    }
}
