package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradeComponentDTO {
    private String name;
    private double score;
    private double weight;
}
