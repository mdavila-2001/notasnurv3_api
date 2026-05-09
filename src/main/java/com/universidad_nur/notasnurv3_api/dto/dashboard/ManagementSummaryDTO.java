package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagementSummaryDTO {
    private Integer id;
    private Integer year;
    private String status; // ACTIVE, CLOSED, CONFIGURING
    private long studentCount;
    private double passRate;
}
