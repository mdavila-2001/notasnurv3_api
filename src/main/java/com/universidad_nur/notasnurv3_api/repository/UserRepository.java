package com.universidad_nur.notasnurv3_api.repository;

import com.universidad_nur.notasnurv3_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Procedure(procedureName = "pr_create_user")
    void createNewUser(
            String p_first_name,
            String p_middle_name,
            String p_last_name_paternal,
            String p_last_name_maternal,
            String p_email,
            String p_pass,
            String p_role
    );

     Optional<User> findByEmail(String email);

     boolean existsByEmail(String email);
}
