package com.universidad_nur.notasnurv3_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.AuditLogResponseDTO;
import com.universidad_nur.notasnurv3_api.services.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponseDTO>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String affectedTable,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 50, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.getAuditLogs(action, affectedTable, search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logs de auditoría obtenidos correctamente", logs));
    }
}
