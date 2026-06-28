package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardTeacherDTO {
    private String welcomeMessage;
    private int pendingActasCount;
    private double averageCourseGrade;
    private List<SubjectSummaryDTO> subjects;
}
