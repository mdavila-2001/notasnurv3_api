package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // 👈 Requerido por JPA
@AllArgsConstructor // 👈 REQUERIDO para el "SELECT new ..."
public class ManagementSummaryDTO {
    private Integer id;
    private Integer year;
    private String status; // ACTIVE, CLOSED, CONFIGURING
    private long studentCount;
    private double passRate;
}