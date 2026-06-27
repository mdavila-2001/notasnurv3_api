package com.universidad_nur.notasnurv3_api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriticalSubjectDTO {
    private String id;
    private String code;
    private String name;
    private String teacherName;
    private double failureRate;
    private String status;
}
