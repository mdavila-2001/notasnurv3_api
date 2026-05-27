package com.universidad_nur.notasnurv3_api.dto;

import java.util.UUID;

import com.universidad_nur.notasnurv3_api.entities.Modality;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {
    
    private String code;
    private String name;
    private Modality modality;
    private Integer capacity;
    private Integer semesterId;
    private UUID teacherId;
    private String category;
    private RecordStatus recordStatus;
}