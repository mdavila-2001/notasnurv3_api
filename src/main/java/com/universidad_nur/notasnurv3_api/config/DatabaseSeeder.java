package com.universidad_nur.notasnurv3_api.config;

import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final DegreeRepository degreeRepository;
    private final ManagementRepository managementRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final UserDegreeRepository userDegreeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("⚡ [SEEDER] Base de datos ya poblada. Omitiendo siembra.");
            return;
        }

        System.out.println("[SEEDER] Iniciando siembra de datos completa...");

        // 1. Facultad
        Faculty faculty = facultyRepository.save(
            Faculty.builder()
                .name("Facultad de Ciencias y Tecnología")
                .code("FCE")
                .build()
        );

        // 2. Carrera
        Degree degree = degreeRepository.save(
            Degree.builder()
                .name("Ingeniería de Sistemas")
                .code("ISC")
                .faculty(faculty)
                .build()
        );

        // 3. Usuarios
        Users admin = userRepository.save(Users.builder()
            .ci("1000000")
            .name("Admin")
            .lastName("Sistema")
            .motherLastName("NUR")
            .email("admin@nur.edu.bo")
            .password(passwordEncoder.encode("admin123"))
            .role(Role.ADMIN)
            .status(UserStatus.ACTIVE)
            .build());

        Users teacher = userRepository.save(Users.builder()
            .ci("2000000")
            .name("Carlos")
            .lastName("Docente")
            .motherLastName("Experto")
            .email("cdocente@nur.edu.bo")
            .password(passwordEncoder.encode("docente123"))
            .role(Role.TEACHER)
            .status(UserStatus.ACTIVE)
            .build());

        Users student = userRepository.save(Users.builder()
            .ci("88997766")
            .name("Maria")
            .lastName("Estudiante")
            .motherLastName("Ejemplo")
            .email("88997766@nur.edu.bo")
            .password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT)
            .status(UserStatus.ACTIVE)
            .build());

        // 4. Gestión y Semestre
        Management management = managementRepository.save(
            Management.builder()
                .year(2026)
                .build()
        );

        Semester semester = semesterRepository.save(
            Semester.builder()
                .number(1)
                .startDate(LocalDate.of(2026, 3, 9))
                .endDate(LocalDate.of(2025, 7, 18))
                .management(management)
                .build()
        );

        // 5. Materia
        Subject subject = subjectRepository.save(
            Subject.builder()
                .code("ISC-301")
                .name("Ingeniería de Software")
                .modality("Presencial")
                .capacity(30)
                .recordStatus(RecordStatus.PUBLISHED)
                .semester(semester)
                .teacher(teacher)
                .build()
        );

        // 6. Expediente Académico (UserDegree)
        UserDegree academicRecord = userDegreeRepository.save(
            UserDegree.builder()
                .user(student)
                .degree(degree)
                .type(ProfileType.STUDENT)
                .status(AcademicStatus.ACTIVE)
                .build()
        );

        // 7. Inscripción (Enrollment)
        enrollmentRepository.save(
            Enrollment.builder()
                .academicRecord(academicRecord)
                .subject(subject)
                .status(EnrollmentStatus.ACTIVE)
                .build()
        );

        // Descontar cupo
        subject.setCapacity(subject.getCapacity() - 1);
        subjectRepository.save(subject);

        System.out.println("✅ [SEEDER] Datos iniciales creados exitosamente.");
        System.out.println("=====================================================");
        System.out.println("🏛️  Facultad : " + faculty.getName());
        System.out.println("🎓  Carrera  : " + degree.getName());
        System.out.println("📚  Materia  : " + subject.getName());
        System.out.println("-----------------------------------------------------");
        System.out.println("🔑 ADMIN     → email: admin@nur.edu.bo | pass: admin123");
        System.out.println("🔑 DOCENTE   → email: cdocente@nur.edu.bo | pass: docente123");
        System.out.println("🔑 ESTUDIANTE→ CI: 88997766 | PIN: 1234");
        System.out.println("=====================================================");
    }
}
