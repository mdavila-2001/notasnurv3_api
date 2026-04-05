package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

@Procedure(procedureName = "pr_create_user")
void createNewUser(
        String p_name,
        String p_middle_name,
        String p_last_name,
        String p_mother_last_name,
        String p_email,
        String p_pass,
        String p_role
);

Optional<Users> findByEmail(String email);
Optional<Users> findByCi(String ci);
}
