package com.universidad_nur.notasnurv3_api.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    List<Users> findByRole(Role role);
    Page<Users> findByRole(Role role, Pageable pageable);
    long countByRole(Role role);

@Procedure(procedureName = "pr_create_user")
void createNewUser(
    @Param("p_ci") String p_ci,
    @Param("p_first_name") String p_name,
    @Param("p_middle_name") String p_mid,
    @Param("p_last_name_paternal") String p_lp,
    @Param("p_last_name_maternal") String p_lm,
    @Param("p_email") String p_email,
    @Param("p_pass") String p_pass,
    @Param("p_role") String p_role
);

    Optional<Users> findByEmail(String email);
    Optional<Users> findByCi(String ci);
}