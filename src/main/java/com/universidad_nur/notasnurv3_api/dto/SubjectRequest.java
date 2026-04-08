package com.universidad_nur.notasnurv3_api.dto;

import java.util.UUID;

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
    private String modality;
    private Integer capacity;
    private Integer semesterId;
    private UUID teacherId;
}