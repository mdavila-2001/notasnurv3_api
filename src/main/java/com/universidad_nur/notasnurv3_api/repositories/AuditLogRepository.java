package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    List<AuditLog> findByAffectedTableAndRecordIdOrderByChangedAtDesc(String affectedTable, UUID recordId);

    @Query("SELECT a, u FROM AuditLog a LEFT JOIN Users u ON a.userId = u.id " +
           "WHERE (:action IS NULL OR a.action = :action) " +
           "AND (:affectedTable IS NULL OR a.affectedTable = :affectedTable) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Object[]> findAllWithFilters(
        @Param("action") String action,
        @Param("affectedTable") String affectedTable,
        @Param("search") String search,
        Pageable pageable
    );
}