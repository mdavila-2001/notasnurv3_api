package com.universidad_nur.notasnurv3_api.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.entities.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByEnrollmentId(UUID enrollmentId);
    Optional<Grade> findByEnrollmentIdAndComponents_Id(UUID enrollmentId, Integer componentId);

    @Transactional
    default List<Grade> bulkSave(List<Grade> grades) {
        return saveAll(grades);
    }
}