package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.SemesterRequest;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import com.universidad_nur.notasnurv3_api.repositories.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class SemesterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManagementRepository managementRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @MockitoSpyBean
    private SemesterRepository semesterRepositorySpy;

    @Test
    void update_debeRetornarBadRequest_cuandoNumeroNoEs1Ni2() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        SemesterRequest request = new SemesterRequest(3, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 6, 15), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Error en los datos enviados"))
            .andExpect(jsonPath("$.data.number").value("El número de semestre debe ser 1 o 2"));
    }

    @Test
    void update_debeRetornarBadRequest_cuandoEndDateNoEsPosteriorAStartDate() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        SemesterRequest request = new SemesterRequest(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("La fecha de fin debe ser posterior a la fecha de inicio."));
    }

    @Test
    void update_debeRetornarBadRequest_cuandoStartDateEsAnteriorAlPrimeroDeEnero() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        SemesterRequest request = new SemesterRequest(1, LocalDate.of(2025, 12, 31), LocalDate.of(2026, 6, 30), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Las fechas del semestre deben estar dentro del año 2026")));
    }

    @Test
    void update_debeRetornarBadRequest_cuandoEndDateEsPosteriorAlTreintaYUnoDeDiciembre() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        SemesterRequest request = new SemesterRequest(1, LocalDate.of(2026, 7, 1), LocalDate.of(2027, 1, 1), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Las fechas del semestre deben estar dentro del año 2026")));
    }

    @Test
    void update_debeRetornarConflict_cuandoNumeroNuevoYaExisteEnLaMismaGestion() throws Exception {
        Management management = guardarManagement(2026);
        Semester semesterUno = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        guardarSemester(management, 2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 15));

        SemesterRequest request = new SemesterRequest(2, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semesterUno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ya existe el semestre 2 para la gestión 2026."));
    }

    @Test
    void update_debeActualizarCorrectamente_cuandoDatosSonValidosYNumeroNoCambia() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        SemesterRequest request = new SemesterRequest(1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semestre actualizado"))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-06-30"))
                .andExpect(jsonPath("$.data.managementId").value(management.getId()))
                .andExpect(jsonPath("$.data.managementYear").value(2026));
    }

    @Test
    void update_debeActualizarCorrectamente_cuandoNumeroCambiaYNoHayDuplicado() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        SemesterRequest request = new SemesterRequest(2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 20), management.getId());

        mockMvc.perform(put("/api/semesters/{id}", semester.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semestre actualizado"))
                .andExpect(jsonPath("$.data.number").value(2))
                .andExpect(jsonPath("$.data.startDate").value("2026-07-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-12-20"));
    }

    @Test
    void delete_debeEliminarCorrectamente() throws Exception {
        Management management = guardarManagement(2026);
        Semester semester = guardarSemester(management, 1, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));

        mockMvc.perform(delete("/api/semesters/{id}", semester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semestre eliminado correctamente"));

        verify(semesterRepositorySpy, times(1)).deleteById(semester.getId());

        mockMvc.perform(get("/api/semesters/{id}", semester.getId()))
            .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Semestre no encontrado con id: " + semester.getId()));
    }

    @Test
    void getById_debeRetornarNotFound_cuandoIdNoExiste() throws Exception {
        mockMvc.perform(get("/api/semesters/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Semestre no encontrado con id: 999999"));
    }

    private Management guardarManagement(Integer year) {
        Management management = new Management();
        management.setYear(year);
        return managementRepository.save(management);
    }

    private Semester guardarSemester(Management management, Integer number, LocalDate startDate, LocalDate endDate) {
        Semester semester = new Semester();
        semester.setManagement(management);
        semester.setNumber(number);
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);
        return semesterRepository.save(semester);
    }

    private String requestJson(SemesterRequest request) {
        return """
                {
                  "number": %d,
                  "startDate": "%s",
                  "endDate": "%s",
                  "managementId": %d
                }
                """.formatted(request.number(), request.startDate(), request.endDate(), request.managementId());
    }
}
