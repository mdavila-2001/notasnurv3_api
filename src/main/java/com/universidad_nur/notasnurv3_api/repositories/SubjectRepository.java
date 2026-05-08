package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    @Procedure(procedureName = "pr_activate_subject")
    void activateSubject(Integer p_subject_id);

    long countByRecordStatus(com.universidad_nur.notasnurv3_api.entities.RecordStatus recordStatus);

    java.util.List<Subject> findBySemesterId(Integer semesterId);
}