package com.universidad_nur.notasnurv3_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponseDTO {
    private Integer id;
    private String code;
    private String name;
    private String modality;
    private Integer capacity;
    private String recordStatus;
    private String semesterName;
    private String teacherName;
    private String management;
}