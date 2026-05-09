package com.universidad_nur.notasnurv3_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagementStatsResponse {
    private Integer managementId;
    private String managementYear;
    private long totalEnrollments;
    private long passedEnrollments;
    private long failedEnrollments;
    private double passRatePercentage;
    private long studentsAtRisk; // A punto de reprobar por faltas
}
