package com.universidad_nur.notasnurv3_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponseDTO(
    UUID id,
    UUID userId,
    String userFullName,
    String userEmail,
    String affectedTable,
    UUID recordId,
    String oldValue,
    String newValue,
    String action,
    LocalDateTime changedAt,
    String ipAddress
) {}
