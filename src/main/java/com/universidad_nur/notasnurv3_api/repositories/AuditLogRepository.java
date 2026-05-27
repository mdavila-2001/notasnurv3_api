package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID; // <-- Importante agregar el UUID

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> { // <-- Cambiamos Long por UUID
    
    // 👇 Adaptado a las nuevas columnas de la base de datos en Neon
    List<AuditLog> findByAffectedTableAndRecordIdOrderByChangedAtDesc(String affectedTable, UUID recordId);
}