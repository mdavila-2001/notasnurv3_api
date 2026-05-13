package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.SubjectResponse;
import com.universidad_nur.notasnurv3_api.entities.Modality;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.services.SubjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = "TEACHER")
class SubjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubjectService subjectService;

    @Test
    void getMySubjects_debeRetornarLasMateriasDelDocente() throws Exception {
        SubjectResponse subject = SubjectResponse.builder()
                .id(10)
                .code("MAT-101")
                .name("Matemática I")
                .modality(Modality.FACE_TO_FACE)
                .capacity(30)
                .recordStatus(RecordStatus.ACTIVE)
                .semesterId(1)
                .semesterName("Semestre 1")
                .teacherId(UUID.randomUUID())
                .teacherName("Docente Prueba")
                .management("2026")
                .category("BASICA")
                .build();

        when(subjectService.getMySubjects(isNull())).thenReturn(List.of(subject));

        mockMvc.perform(get("/api/subjects/my-subjects"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].name").value("Matemática I"))
            .andExpect(jsonPath("$[0].teacherName").value("Docente Prueba"));
    }
}