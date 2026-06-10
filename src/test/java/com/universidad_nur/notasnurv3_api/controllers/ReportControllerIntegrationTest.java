package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = "TEACHER")
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void downloadGradesReportPdf_debeRetornarPdfBytes() throws Exception {
        byte[] pdfBytes = "Dummy PDF Content".getBytes();
        when(reportService.generateActaNotasPdf(anyInt())).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/reports/subjects/1/grades-report/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"Acta_Notas_1.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void downloadAttendanceExcel_debeRetornarExcelBytes() throws Exception {
        byte[] excelBytes = "Dummy Excel Content".getBytes();
        when(reportService.generateAsistenciaExcel(anyInt())).thenReturn(excelBytes);

        mockMvc.perform(get("/api/reports/subjects/1/attendance/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"Asistencias_1.xlsx\""))
                .andExpect(content().bytes(excelBytes));
    }
}
