package com.universidad_nur.notasnurv3_api.config;

import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("[SEEDER] Iniciando siembra de usuarios de prueba...");

            // 1. Cuenta de ADMINISTRADOR
            Users admin = Users.builder()
                    .ci("1000000") // CI falso
                    .name("Admin")
                    .lastName("Sistema")
                    .motherLastName("NUR")
                    .email("admin@nur.edu.bo")
                    .password(passwordEncoder.encode("admin123")) // Contraseña: admin123
                    .role(Role.ADMIN)
                    .status("ACTIVE")
                    .build();

            // 2. Cuenta de DOCENTE
            Users teacher = Users.builder()
                    .ci("2000000")
                    .name("Carlos")
                    .lastName("Docente")
                    .motherLastName("Experto")
                    .email("cdocente@nur.edu.bo")
                    .password(passwordEncoder.encode("docente123")) // Contraseña: docente123
                    .role(Role.TEACHER)
                    .status("ACTIVE")
                    .build();

            // 3. Cuenta de ESTUDIANTE
            Users student = Users.builder()
                    .ci("88997766") // Este es el CI con el que hará Login
                    .name("Maria")
                    .lastName("Estudiante")
                    .motherLastName("Ejemplo")
                    .email("88997766@nur.edu.bo") // Alumno usa email otorgado por administración que viene a ser su CI de estudiante
                    .password(passwordEncoder.encode("1234")) // PIN corto para el estudiante
                    .role(Role.STUDENT)
                    .status("ACTIVE")
                    .build();

            // Guardamos todos en lote
            userRepository.saveAll(List.of(admin, teacher, student));

            System.out.println("✅ [SEEDER] 3 usuarios creados exitosamente.");
            System.out.println("🔑 ADMIN -> Correo: admin@nur.edu.bo | Pass: admin123");
            System.out.println("🔑 DOCENTE -> Correo: cdocente@nur.edu.bo | Pass: docente123");
            System.out.println("🔑 ESTUDIANTE -> CI: 88997766 | PIN: 1234");
            System.out.println("=====================================================");
        } else {
            System.out.println("⚡ [SEEDER] Base de datos ya poblada. Omitiendo siembra.");
        }
    }
}
