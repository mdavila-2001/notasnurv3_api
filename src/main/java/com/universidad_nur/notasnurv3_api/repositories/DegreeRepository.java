package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, Integer> {
    boolean existsByCode(String code);
}
