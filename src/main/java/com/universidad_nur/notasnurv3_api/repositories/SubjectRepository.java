package com.universidad_nur.notasnurv3_api.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.universidad_nur.notasnurv3_api.entities.Subject;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    @Procedure(procedureName = "pr_activate_subject")
    void activateSubject(Integer p_subject_id);

    Optional<Subject> findByCode(String code);
    List<Subject> findByTeacherId(UUID teacherId);
    List<Subject> findBySemesterId(Integer semesterId);
}