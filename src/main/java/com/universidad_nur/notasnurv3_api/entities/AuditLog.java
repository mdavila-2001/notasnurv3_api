package com.universidad_nur.notasnurv3_api.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName; // "GRADE" o "ATTENDANCE"

    @Column(name = "entity_ref_id", nullable = false, length = 50)
    private String entityRefId; // ID de la nota o la asistencia

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // "CREATE" o "UPDATE"

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy; // Email del docente o administrador

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
