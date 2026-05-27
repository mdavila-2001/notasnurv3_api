package com.universidad_nur.notasnurv3_api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    // --- COLUMNAS NUEVAS DE NEON ---
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "affected_table", nullable = false, length = 255)
    private String affectedTable; 

    @Column(name = "record_id", nullable = false)
    private UUID recordId; 

    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue; 

    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue; 

    @Column(name = "action", nullable = false, length = 255)
    private String action; 

    @Column(name = "changed_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(name = "ip_address", length = 255)
    private String ipAddress;

    // --- COLUMNAS VIEJAS (Que la BD aún exige que no sean nulas) ---
    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    @Column(name = "entity_ref_id", nullable = false, length = 50)
    private String entityRefId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    // 👇 SOLUCIÓN: El sobreviviente inesperado
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}