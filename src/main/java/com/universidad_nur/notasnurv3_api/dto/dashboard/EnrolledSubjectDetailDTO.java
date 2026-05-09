package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EnrolledSubjectDetailDTO {
    private Integer subjectId;
    private String subjectName;
    private String subjectCode;
    private String teacherName;
    private String category;
    private double currentGrade;
    private int absences;
    private int absenceLimit;
    private boolean atRisk;
    private List<GradeComponentDTO> gradeBreakdown;
}
