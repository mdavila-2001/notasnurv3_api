package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardAdminDTO {
    private long totalStudents;
    private long totalSubjectsWithoutTeacher;
    private long totalOpenActas;
    private double globalPassRate;
    private long studentsAtRiskCount;
    private List<ManagementSummaryDTO> managements;
    private List<CriticalSubjectDTO> criticalSubjects;
}

