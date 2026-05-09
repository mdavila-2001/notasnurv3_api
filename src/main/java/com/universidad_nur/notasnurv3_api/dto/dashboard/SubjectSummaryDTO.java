package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectSummaryDTO {
    private Integer id;
    private String code;
    private String name;
    private String modality;
    private int studentCount;
    private double progressPercentage;
}
