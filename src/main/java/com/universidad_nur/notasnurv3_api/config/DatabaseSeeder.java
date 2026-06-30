package com.universidad_nur.notasnurv3_api.config;

import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.UserStatus;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.count() > 0) {
            log.info("Base de datos ya poblada. Omitiendo siembra.");
            return;
        }

        log.info("============================================");
        log.info("  SEMBRANDO USUARIO ADMINISTRADOR INICIAL");
        log.info("============================================");

        userRepository.save(Users.builder()
            .ci("1000000")
            .name("Admin")
            .lastName("Sistema")
            .motherLastName("NUR")
            .email("admin@nur.edu.bo")
            .password(passwordEncoder.encode("admin123"))
            .role(Role.ADMIN)
            .status(UserStatus.ACTIVE)
            .build());

        log.info("   Administrador creado → admin@nur.edu.bo");
        log.info("   Nota: por seguridad no se registran contraseñas en logs.");
        log.info("============================================");
    }
}
