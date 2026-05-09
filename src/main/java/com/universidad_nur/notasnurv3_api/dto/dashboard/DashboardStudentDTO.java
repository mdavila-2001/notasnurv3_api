package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardStudentDTO {
    private String studentName;
    private String degreeName;
    private double currentGPA;
    private List<EnrolledSubjectDetailDTO> enrolledSubjects;
}
