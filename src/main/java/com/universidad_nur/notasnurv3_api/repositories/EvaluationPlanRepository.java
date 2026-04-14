package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationPlanRepository extends JpaRepository<EvaluationPlan, Integer> {
    Optional<EvaluationPlan> findBySubjectId(Integer subjectId);
}
