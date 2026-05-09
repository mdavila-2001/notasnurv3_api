package com.universidad_nur.notasnurv3_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDegreeResponse {
    private Integer id;
    private String studentName;
    private String degreeName;
    private String type;
    private String status;
}