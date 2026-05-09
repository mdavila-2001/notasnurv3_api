package com.universidad_nur.notasnurv3_api.dto;

import com.universidad_nur.notasnurv3_api.entities.Modality;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {
    private Integer id;
    private String code;
    private String name;
    private Modality modality;
    private Integer capacity;
    private RecordStatus recordStatus;
    private Integer semesterId;
    private String semesterName;
    private UUID teacherId;
    private String teacherName;
    private String management;
    private String category;
}