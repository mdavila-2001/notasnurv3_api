package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, Integer> {
    boolean existsByCode(String code);
    boolean existsByName(String name);
    boolean existsByCodeAndIdNot(String code, Integer id);
    boolean existsByNameAndIdNot(String name, Integer id);
    
    @Query("SELECT d FROM Degree d LEFT JOIN FETCH d.faculty")
    List<Degree> findAllWithFaculty();
    
    @Query("SELECT d FROM Degree d LEFT JOIN FETCH d.faculty WHERE d.id = :id")
    Optional<Degree> findByIdWithFaculty(@Param("id") Integer id);
    
    long countByFaculty_Id(Integer facultyId);
}
