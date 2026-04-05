package com.universidad_nur.notasnurv3_api.repositories;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    
}