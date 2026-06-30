package com.universidad_nur.notasnurv3_api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.AuditLogResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.AuditLog;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLogResponseDTO> getAuditLogs(String action, String affectedTable, String search, Pageable pageable) {
        String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
        String actionParam = (action == null || action.trim().isEmpty()) ? null : action.trim();
        String tableParam = (affectedTable == null || affectedTable.trim().isEmpty()) ? null : affectedTable.trim();

        Page<Object[]> results = auditLogRepository.findAllWithFilters(actionParam, tableParam, searchParam, pageable);
        
        return results.map(row -> {
            AuditLog auditLog = (AuditLog) row[0];
            Users user = (Users) row[1];
            String userFullName = user != null ? user.getFullName() : "Sistema";
            String userEmail = user != null ? user.getEmail() : "system@nur.edu";
            
            return new AuditLogResponseDTO(
                auditLog.getId(),
                auditLog.getUserId(),
                userFullName,
                userEmail,
                auditLog.getAffectedTable(),
                auditLog.getRecordId(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getAction(),
                auditLog.getChangedAt(),
                auditLog.getIpAddress()
            );
        });
    }
}
