package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDegreeRepository extends JpaRepository<UserDegree, Integer> {
    // Busca si ya existe un expediente para ese usuario en esa carrera
    boolean existsByUserIdAndDegreeId(UUID userId, Integer degreeId);

    // Trae todos los expedientes de un usuario (útil para Rodrigo más adelante)
    List<UserDegree> findByUserId(UUID userId);
}
