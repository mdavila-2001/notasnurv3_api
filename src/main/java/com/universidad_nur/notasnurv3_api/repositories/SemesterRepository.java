package com.universidad_nur.notasnurv3_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer> {
    boolean existsByManagementAndNumber(Management management, Integer number);

    List<Semester> findByManagementId(Integer managementId);
}