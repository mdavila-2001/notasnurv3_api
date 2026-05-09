package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationPlanRepository extends JpaRepository<EvaluationPlan, Integer> {
    Optional<EvaluationPlan> findBySubjectId(Integer subjectId);

    @Query("""
            SELECT DISTINCT ep
            FROM EvaluationPlan ep
            LEFT JOIN FETCH ep.components c
            WHERE ep.subject.id IN :subjectIds
            """)
    List<EvaluationPlan> findBySubjectIdIn(@Param("subjectIds") Collection<Integer> subjectIds);
}
