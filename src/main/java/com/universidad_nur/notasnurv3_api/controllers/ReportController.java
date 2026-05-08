package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/subjects/{id}/acta-notas/pdf")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN) or hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<byte[]> downloadActaNotasPdf(@PathVariable Integer id) {
        byte[] pdfBytes = reportService.generateActaNotasPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Acta_Notas_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/subjects/{id}/asistencia/excel")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN) or hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<byte[]> downloadAsistenciaExcel(@PathVariable Integer id) {
        byte[] excelBytes = reportService.generateAsistenciaExcel(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Asistencias_" + id + ".xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
