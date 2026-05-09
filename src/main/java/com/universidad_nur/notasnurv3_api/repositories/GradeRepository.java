package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByEnrollmentId(UUID enrollmentId);
    Optional<Grade> findByEnrollmentIdAndComponentId(UUID enrollmentId, Integer componentId);
    
    @Transactional
    default List<Grade> bulkSave(List<Grade> grades) {
        return saveAll(grades);
    }
}
