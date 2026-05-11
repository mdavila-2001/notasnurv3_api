package com.universidad_nur.notasnurv3_api.config;

import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final DegreeRepository degreeRepository;
    private final ManagementRepository managementRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final UserDegreeRepository userDegreeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EvaluationPlanRepository evaluationPlanRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.count() > 0) {
            log.info("Base de datos ya poblada. Omitiendo siembra.");
            return;
        }

        log.info("============================================");
        log.info("  INICIANDO SIEMBRA DE DATOS COMPLETA");
        log.info("============================================");

        // ========== 1. FACULTADES Y CARRERAS ==========
        Faculty fce = facultyRepository.save(
            Faculty.builder().name("Facultad de Ciencias y Tecnología").code("FCE").build());
        Faculty salud = facultyRepository.save(
            Faculty.builder().name("Facultad de Ciencias de la Salud").code("SAL").build());

        Degree isc = degreeRepository.save(
            Degree.builder().name("Ingeniería de Sistemas").code("ISC").faculty(fce).build());
        Degree civ = degreeRepository.save(
            Degree.builder().name("Ingeniería Civil").code("CIV").faculty(fce).build());
        Degree med = degreeRepository.save(
            Degree.builder().name("Medicina").code("MED").faculty(salud).build());

        // ========== 2. USUARIOS ==========
        // Admin
        userRepository.save(Users.builder()
            .ci("1000000").name("Admin").lastName("Sistema").motherLastName("NUR")
            .email("admin@nur.edu.bo").password(passwordEncoder.encode("admin123"))
            .role(Role.ADMIN).status(UserStatus.ACTIVE).build());

        // 3 Docentes
        Users t1 = userRepository.save(Users.builder()
            .ci("2000000").name("Carlos").lastName("Docente").motherLastName("Experto")
            .email("cdocente@nur.edu.bo").password(passwordEncoder.encode("docente123"))
            .role(Role.TEACHER).status(UserStatus.ACTIVE).build());

        Users t2 = userRepository.save(Users.builder()
            .ci("2000001").name("Maria").middleName("Elena").lastName("Lopez").motherLastName("Garcia")
            .email("mlopez@nur.edu.bo").password(passwordEncoder.encode("docente123"))
            .role(Role.TEACHER).status(UserStatus.ACTIVE).build());

        Users t3 = userRepository.save(Users.builder()
            .ci("2000002").name("Juan").lastName("Perez").motherLastName("Choque")
            .email("jperez@nur.edu.bo").password(passwordEncoder.encode("docente123"))
            .role(Role.TEACHER).status(UserStatus.ACTIVE).build());

        // 8 Estudiantes
        Users s1 = userRepository.save(Users.builder()
            .ci("88997766").name("Maria").lastName("Estudiante").motherLastName("Ejemplo")
            .email("88997766@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s2 = userRepository.save(Users.builder()
            .ci("12345678").name("Pedro").middleName("Andres").lastName("Ramirez").motherLastName("Flores")
            .email("12345678@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s3 = userRepository.save(Users.builder()
            .ci("23456789").name("Ana").lastName("Condori").motherLastName("Mamani")
            .email("23456789@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s4 = userRepository.save(Users.builder()
            .ci("34567890").name("Luis").middleName("Alberto").lastName("Quispe").motherLastName("Huanca")
            .email("34567890@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s5 = userRepository.save(Users.builder()
            .ci("45678901").name("Sofia").lastName("Vargas").motherLastName("Rojas")
            .email("45678901@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s6 = userRepository.save(Users.builder()
            .ci("56789012").name("Diego").middleName("Alejandro").lastName("Rodriguez").motherLastName("Paredes")
            .email("56789012@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s7 = userRepository.save(Users.builder()
            .ci("67890123").name("Camila").lastName("Fernandez").motherLastName("Lopez")
            .email("67890123@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        Users s8 = userRepository.save(Users.builder()
            .ci("78901234").name("Jose").middleName("Antonio").lastName("Martinez").motherLastName("Cruz")
            .email("78901234@nur.edu.bo").password(passwordEncoder.encode("1234"))
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build());

        // ========== 3. EXPEDIENTES ACADEMICOS (UserDegree) ==========
        userDegreeRepository.save(UserDegree.builder().user(t1).degree(isc).type(ProfileType.TEACHER).status(AcademicStatus.ACTIVE).build());
        userDegreeRepository.save(UserDegree.builder().user(t2).degree(civ).type(ProfileType.TEACHER).status(AcademicStatus.ACTIVE).build());
        userDegreeRepository.save(UserDegree.builder().user(t3).degree(med).type(ProfileType.TEACHER).status(AcademicStatus.ACTIVE).build());

        UserDegree udS1 = userDegreeRepository.save(UserDegree.builder().user(s1).degree(isc).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS2 = userDegreeRepository.save(UserDegree.builder().user(s2).degree(isc).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS3 = userDegreeRepository.save(UserDegree.builder().user(s3).degree(isc).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS4 = userDegreeRepository.save(UserDegree.builder().user(s4).degree(civ).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS5 = userDegreeRepository.save(UserDegree.builder().user(s5).degree(civ).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS6 = userDegreeRepository.save(UserDegree.builder().user(s6).degree(med).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS7 = userDegreeRepository.save(UserDegree.builder().user(s7).degree(med).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());
        UserDegree udS8 = userDegreeRepository.save(UserDegree.builder().user(s8).degree(isc).type(ProfileType.STUDENT).status(AcademicStatus.ACTIVE).build());

        // ========== 4. GESTIONES Y SEMESTRES ==========
        Management m2025 = managementRepository.save(Management.builder().year(2025).build());
        Management m2026 = managementRepository.save(Management.builder().year(2026).build());

        Semester sem2025_1 = semesterRepository.save(
            Semester.builder().number(1).startDate(LocalDate.of(2025, 2, 1)).endDate(LocalDate.of(2025, 6, 30)).management(m2025).build());
        semesterRepository.save(
            Semester.builder().number(2).startDate(LocalDate.of(2025, 8, 1)).endDate(LocalDate.of(2025, 12, 15)).management(m2025).build());
        Semester sem2026_1 = semesterRepository.save(
            Semester.builder().number(1).startDate(LocalDate.of(2026, 3, 9)).endDate(LocalDate.of(2026, 7, 18)).management(m2026).build());

        // ========== 5. CONFIGURACIONES DEL SISTEMA ==========
        systemSettingRepository.save(SystemSetting.builder().settingKey("MAX_ABSENCES_PRESENTIAL").settingValue("5").description("Limite de faltas para modalidad presencial").build());
        systemSettingRepository.save(SystemSetting.builder().settingKey("MAX_ABSENCES_BLENDED").settingValue("3").description("Limite de faltas para modalidad semipresencial").build());
        systemSettingRepository.save(SystemSetting.builder().settingKey("NUR_ROUNDING_MODE").settingValue("HALF_UP").description("Modo de redondeo de notas").build());

        // ========== 6. MATERIAS ==========
        // Teacher 1: Carlos -> ISC
        Subject isc301 = subjectRepository.save(Subject.builder()
            .code("ISC-301").name("Ingeniería de Software").modality(Modality.FACE_TO_FACE)
            .capacity(30).recordStatus(RecordStatus.ACTIVE).semester(sem2026_1).teacher(t1).category("ESPECIALIDAD").build());
        Subject isc302 = subjectRepository.save(Subject.builder()
            .code("ISC-302").name("Base de Datos").modality(Modality.FACE_TO_FACE)
            .capacity(35).recordStatus(RecordStatus.PUBLISHED).semester(sem2026_1).teacher(t1).category("ESPECIALIDAD").build());
        Subject isc101 = subjectRepository.save(Subject.builder()
            .code("ISC-101").name("Programación I").modality(Modality.FACE_TO_FACE)
            .capacity(30).recordStatus(RecordStatus.CLOSED).semester(sem2025_1).teacher(t1).category("BASICA").build());

        // Teacher 2: Maria -> CIV
        Subject civ201 = subjectRepository.save(Subject.builder()
            .code("CIV-201").name("Resistencia de Materiales").modality(Modality.FACE_TO_FACE)
            .capacity(25).recordStatus(RecordStatus.ACTIVE).semester(sem2026_1).teacher(t2).category("ESPECIALIDAD").build());
        subjectRepository.save(Subject.builder()
            .code("CIV-202").name("Topografía").modality(Modality.BLENDED)
            .capacity(20).recordStatus(RecordStatus.DRAFT).semester(sem2026_1).teacher(t2).category("ESPECIALIDAD").build());

        // Teacher 3: Juan -> MED
        Subject med101 = subjectRepository.save(Subject.builder()
            .code("MED-101").name("Anatomía Humana").modality(Modality.FACE_TO_FACE)
            .capacity(40).recordStatus(RecordStatus.ACTIVE).semester(sem2026_1).teacher(t3).category("BASICA").build());
        Subject med201 = subjectRepository.save(Subject.builder()
            .code("MED-201").name("Farmacología General").modality(Modality.FACE_TO_FACE)
            .capacity(35).recordStatus(RecordStatus.PUBLISHED).semester(sem2026_1).teacher(t3).category("ESPECIALIDAD").build());

        // Sin docente (para probar metrica)
        subjectRepository.save(Subject.builder()
            .code("ISC-401").name("Inteligencia Artificial").modality(Modality.ONLINE)
            .capacity(30).recordStatus(RecordStatus.DRAFT).semester(sem2026_1).teacher(t1).category("ESPECIALIDAD").build());

        // ========== 7. PLANES DE EVALUACION Y COMPONENTES ==========
        // ISC-301: 4 componentes
        List<Components> rawIsc301 = new ArrayList<>(List.of(
            Components.builder().name("Parcial 1").weight(new BigDecimal("25")).build(),
            Components.builder().name("Parcial 2").weight(new BigDecimal("25")).build(),
            Components.builder().name("Examen Final").weight(new BigDecimal("30")).build(),
            Components.builder().name("Laboratorio").weight(new BigDecimal("20")).build()
        ));
        EvaluationPlan planIsc301 = EvaluationPlan.builder().subject(isc301).components(rawIsc301).build();
        rawIsc301.forEach(c -> c.setPlan(planIsc301));
        evaluationPlanRepository.save(planIsc301);
        List<Components> compIsc301 = planIsc301.getComponents();

        // ISC-302: 4 componentes
        List<Components> rawIsc302 = new ArrayList<>(List.of(
            Components.builder().name("Práctico 1").weight(new BigDecimal("15")).build(),
            Components.builder().name("Práctico 2").weight(new BigDecimal("15")).build(),
            Components.builder().name("Parcial").weight(new BigDecimal("30")).build(),
            Components.builder().name("Examen Final").weight(new BigDecimal("40")).build()
        ));
        EvaluationPlan planIsc302 = EvaluationPlan.builder().subject(isc302).components(rawIsc302).build();
        rawIsc302.forEach(c -> c.setPlan(planIsc302));
        evaluationPlanRepository.save(planIsc302);
        List<Components> compIsc302 = planIsc302.getComponents();

        // CIV-201: 3 componentes
        List<Components> rawCiv201 = new ArrayList<>(List.of(
            Components.builder().name("Parcial").weight(new BigDecimal("30")).build(),
            Components.builder().name("Trabajo Práctico").weight(new BigDecimal("20")).build(),
            Components.builder().name("Examen Final").weight(new BigDecimal("50")).build()
        ));
        EvaluationPlan planCiv201 = EvaluationPlan.builder().subject(civ201).components(rawCiv201).build();
        rawCiv201.forEach(c -> c.setPlan(planCiv201));
        evaluationPlanRepository.save(planCiv201);
        List<Components> compCiv201 = planCiv201.getComponents();

        // MED-101: 3 componentes
        List<Components> rawMed101 = new ArrayList<>(List.of(
            Components.builder().name("Parcial Teórico").weight(new BigDecimal("40")).build(),
            Components.builder().name("Parcial Práctico").weight(new BigDecimal("30")).build(),
            Components.builder().name("Examen Final").weight(new BigDecimal("30")).build()
        ));
        EvaluationPlan planMed101 = EvaluationPlan.builder().subject(med101).components(rawMed101).build();
        rawMed101.forEach(c -> c.setPlan(planMed101));
        evaluationPlanRepository.save(planMed101);
        List<Components> compMed101 = planMed101.getComponents();

        // MED-201: 3 componentes
        List<Components> rawMed201 = new ArrayList<>(List.of(
            Components.builder().name("Prácticas").weight(new BigDecimal("20")).build(),
            Components.builder().name("Parcial").weight(new BigDecimal("30")).build(),
            Components.builder().name("Examen Final").weight(new BigDecimal("50")).build()
        ));
        EvaluationPlan planMed201 = EvaluationPlan.builder().subject(med201).components(rawMed201).build();
        rawMed201.forEach(c -> c.setPlan(planMed201));
        evaluationPlanRepository.save(planMed201);
        List<Components> compMed201 = planMed201.getComponents();

        // ========== 8. INSCRIPCIONES (Enrollments) ==========
        // --- Historicas 2025 ---
        Enrollment encIsc101_S1 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS1).subject(isc101).status(EnrollmentStatus.PASSED).finalScore(78).build());
        Enrollment encIsc101_S2 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS2).subject(isc101).status(EnrollmentStatus.PASSED).finalScore(82).build());
        Enrollment encIsc101_S3 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS3).subject(isc101).status(EnrollmentStatus.FAILED).finalScore(45).build());
        Enrollment encIsc101_S8 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS8).subject(isc101).status(EnrollmentStatus.PASSED).finalScore(91).build());

        // --- Activas 2026 ---
        // ISC-301
        Enrollment encIsc301_S1 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS1).subject(isc301).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encIsc301_S2 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS2).subject(isc301).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encIsc301_S3 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS3).subject(isc301).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encIsc301_S8 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS8).subject(isc301).status(EnrollmentStatus.ACTIVE).build());

        // ISC-302
        Enrollment encIsc302_S1 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS1).subject(isc302).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encIsc302_S2 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS2).subject(isc302).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encIsc302_S8 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS8).subject(isc302).status(EnrollmentStatus.ACTIVE).build());

        // CIV-201
        Enrollment encCiv201_S4 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS4).subject(civ201).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encCiv201_S5 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS5).subject(civ201).status(EnrollmentStatus.ACTIVE).build());

        // MED-101
        Enrollment encMed101_S6 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS6).subject(med101).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encMed101_S7 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS7).subject(med101).status(EnrollmentStatus.ACTIVE).build());

        // MED-201
        Enrollment encMed201_S6 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS6).subject(med201).status(EnrollmentStatus.ACTIVE).build());
        Enrollment encMed201_S7 = enrollmentRepository.save(Enrollment.builder()
            .academicRecord(udS7).subject(med201).status(EnrollmentStatus.ACTIVE).build());

        // ========== 9. NOTAS (Grades) ==========
        // --- Historicas ---
        // ISC-101 (no tiene plan de evaluacion en el seeder, pero tiene finalScore)
        // Las notas historicas por componente no son relevantes, solo el finalScore. Omitimos grades individuales.

        // --- ISC-301 (4 componentes) ---
        // S1: P1=20, P2=22, EF=25, Lab=18 -> 85
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S1).components(compIsc301.get(0)).teacher(t1).score(new BigDecimal("20")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S1).components(compIsc301.get(1)).teacher(t1).score(new BigDecimal("22")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S1).components(compIsc301.get(2)).teacher(t1).score(new BigDecimal("25")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S1).components(compIsc301.get(3)).teacher(t1).score(new BigDecimal("18")).build());
        // S2: P1=15, P2=18, EF=20, Lab=15 -> 68
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S2).components(compIsc301.get(0)).teacher(t1).score(new BigDecimal("15")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S2).components(compIsc301.get(1)).teacher(t1).score(new BigDecimal("18")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S2).components(compIsc301.get(2)).teacher(t1).score(new BigDecimal("20")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S2).components(compIsc301.get(3)).teacher(t1).score(new BigDecimal("15")).build());
        // S3: P1=10, P2=12, EF=15, Lab=10 -> 47 (en riesgo academico)
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S3).components(compIsc301.get(0)).teacher(t1).score(new BigDecimal("10")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S3).components(compIsc301.get(1)).teacher(t1).score(new BigDecimal("12")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S3).components(compIsc301.get(2)).teacher(t1).score(new BigDecimal("15")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S3).components(compIsc301.get(3)).teacher(t1).score(new BigDecimal("10")).build());
        // S8: P1=23, P2=24, EF=28, Lab=19 -> 94
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S8).components(compIsc301.get(0)).teacher(t1).score(new BigDecimal("23")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S8).components(compIsc301.get(1)).teacher(t1).score(new BigDecimal("24")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S8).components(compIsc301.get(2)).teacher(t1).score(new BigDecimal("28")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc301_S8).components(compIsc301.get(3)).teacher(t1).score(new BigDecimal("19")).build());

        // --- ISC-302 (4 componentes) ---
        // S1: Pr1=12, Pr2=13, Par=25, EF=35 -> 85
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S1).components(compIsc302.get(0)).teacher(t1).score(new BigDecimal("12")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S1).components(compIsc302.get(1)).teacher(t1).score(new BigDecimal("13")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S1).components(compIsc302.get(2)).teacher(t1).score(new BigDecimal("25")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S1).components(compIsc302.get(3)).teacher(t1).score(new BigDecimal("35")).build());
        // S2: Pr1=10, Pr2=11, Par=22, EF=30 -> 73
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S2).components(compIsc302.get(0)).teacher(t1).score(new BigDecimal("10")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S2).components(compIsc302.get(1)).teacher(t1).score(new BigDecimal("11")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S2).components(compIsc302.get(2)).teacher(t1).score(new BigDecimal("22")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S2).components(compIsc302.get(3)).teacher(t1).score(new BigDecimal("30")).build());
        // S8: Pr1=14, Pr2=14, Par=28, EF=38 -> 94
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S8).components(compIsc302.get(0)).teacher(t1).score(new BigDecimal("14")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S8).components(compIsc302.get(1)).teacher(t1).score(new BigDecimal("14")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S8).components(compIsc302.get(2)).teacher(t1).score(new BigDecimal("28")).build());
        gradeRepository.save(Grade.builder().enrollment(encIsc302_S8).components(compIsc302.get(3)).teacher(t1).score(new BigDecimal("38")).build());

        // --- CIV-201 (3 componentes) ---
        // S4: Par=22, TP=18, EF=40 -> 80
        gradeRepository.save(Grade.builder().enrollment(encCiv201_S4).components(compCiv201.get(0)).teacher(t2).score(new BigDecimal("22")).build());
        gradeRepository.save(Grade.builder().enrollment(encCiv201_S4).components(compCiv201.get(1)).teacher(t2).score(new BigDecimal("18")).build());
        gradeRepository.save(Grade.builder().enrollment(encCiv201_S4).components(compCiv201.get(2)).teacher(t2).score(new BigDecimal("40")).build());
        // S5: Par=25, TP=19, EF=45 -> 89
        gradeRepository.save(Grade.builder().enrollment(encCiv201_S5).components(compCiv201.get(0)).teacher(t2).score(new BigDecimal("25")).build());
        gradeRepository.save(Grade.builder().enrollment(encCiv201_S5).components(compCiv201.get(1)).teacher(t2).score(new BigDecimal("19")).build());
        gradeRepository.save(Grade.builder().enrollment(encCiv201_S5).components(compCiv201.get(2)).teacher(t2).score(new BigDecimal("45")).build());

        // --- MED-101 (3 componentes) ---
        // S6: PT=30, PP=25, EF=25 -> 80
        gradeRepository.save(Grade.builder().enrollment(encMed101_S6).components(compMed101.get(0)).teacher(t3).score(new BigDecimal("30")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed101_S6).components(compMed101.get(1)).teacher(t3).score(new BigDecimal("25")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed101_S6).components(compMed101.get(2)).teacher(t3).score(new BigDecimal("25")).build());
        // S7: PT=35, PP=28, EF=28 -> 91
        gradeRepository.save(Grade.builder().enrollment(encMed101_S7).components(compMed101.get(0)).teacher(t3).score(new BigDecimal("35")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed101_S7).components(compMed101.get(1)).teacher(t3).score(new BigDecimal("28")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed101_S7).components(compMed101.get(2)).teacher(t3).score(new BigDecimal("28")).build());

        // --- MED-201 (3 componentes) ---
        // S6: Prac=15, Par=25, EF=40 -> 80
        gradeRepository.save(Grade.builder().enrollment(encMed201_S6).components(compMed201.get(0)).teacher(t3).score(new BigDecimal("15")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed201_S6).components(compMed201.get(1)).teacher(t3).score(new BigDecimal("25")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed201_S6).components(compMed201.get(2)).teacher(t3).score(new BigDecimal("40")).build());
        // S7: Prac=18, Par=27, EF=45 -> 90
        gradeRepository.save(Grade.builder().enrollment(encMed201_S7).components(compMed201.get(0)).teacher(t3).score(new BigDecimal("18")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed201_S7).components(compMed201.get(1)).teacher(t3).score(new BigDecimal("27")).build());
        gradeRepository.save(Grade.builder().enrollment(encMed201_S7).components(compMed201.get(2)).teacher(t3).score(new BigDecimal("45")).build());

        // ========== 10. ASISTENCIAS ==========
        LocalDate[] isc301Dates = {
            LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 23),
            LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 13),
            LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 27),
            LocalDate.of(2026, 5, 4)
        };
        // Ana (S3) con 4 faltas -> en riesgo (limite 5, falta 1 más)
        int[] anaAbsentIndexes = {0, 1, 3, 5};
        for (int i = 0; i < isc301Dates.length; i++) {
            for (Enrollment e : List.of(encIsc301_S1, encIsc301_S2, encIsc301_S3, encIsc301_S8)) {
                boolean isAna = e.getAcademicRecord().getUser().getCi().equals("23456789");
                boolean isAbsent = isAna && contains(anaAbsentIndexes, i);
                attendanceRepository.save(Attendance.builder()
                    .enrollment(e).date(isc301Dates[i])
                    .status(isAbsent ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT)
                    .build());
            }
        }

        LocalDate[] civ201Dates = {
            LocalDate.of(2026, 3, 17), LocalDate.of(2026, 3, 31),
            LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 28),
            LocalDate.of(2026, 5, 5)
        };
        // Luis (S4) 1 falta
        for (int i = 0; i < civ201Dates.length; i++) {
            boolean isLuisAbsent = i == 0;
            attendanceRepository.save(Attendance.builder()
                .enrollment(encCiv201_S4).date(civ201Dates[i])
                .status(isLuisAbsent ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder()
                .enrollment(encCiv201_S5).date(civ201Dates[i])
                .status(AttendanceStatus.PRESENT).build());
        }

        LocalDate[] med101Dates = {
            LocalDate.of(2026, 3, 18), LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 29),
            LocalDate.of(2026, 5, 6)
        };
        // Camila (S7) 2 faltas -> no en riesgo (2 < 4)
        int[] camilaAbsentIndexes = {1, 3};
        for (int i = 0; i < med101Dates.length; i++) {
            for (Enrollment e : List.of(encMed101_S6, encMed101_S7)) {
                boolean isCamila = e.getAcademicRecord().getUser().getCi().equals("67890123");
                boolean isAbsent = isCamila && contains(camilaAbsentIndexes, i);
                attendanceRepository.save(Attendance.builder()
                    .enrollment(e).date(med101Dates[i])
                    .status(isAbsent ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT)
                    .build());
            }
        }

        LocalDate[] med201Dates = {
            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 4, 3),
            LocalDate.of(2026, 4, 17), LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 8)
        };
        for (LocalDate date : med201Dates) {
            attendanceRepository.save(Attendance.builder().enrollment(encMed201_S6).date(date).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().enrollment(encMed201_S7).date(date).status(AttendanceStatus.PRESENT).build());
        }

        log.info("============================================");
        log.info("   SIEMBRA COMPLETADA EXITOSAMENTE");
        log.info("============================================");
        log.info("   Total facultades : 2");
        log.info("   Total carreras   : 3");
        log.info("   Total usuarios   : 12 (1 admin, 3 docentes, 8 estudiantes)");
        log.info("   Total gestiones  : 2 (2025 cerrada, 2026 activa)");
        log.info("   Total materias   : 8");
        log.info("   Total planes eval: 5");
        log.info("   Total incripciones: 17 (13 activas, 4 historicas)");
        log.info("   Total notas      : 46");
        log.info("   Total asistencias: ~58");
        log.info("============================================");
        log.info("   ACCESOS:");
        log.info("   ADMIN   → admin@nur.edu.bo / admin123");
        log.info("   DOCENTE → cdocente@nur.edu.bo / docente123  (Carlos - ISC)");
        log.info("   DOCENTE → mlopez@nur.edu.bo / docente123    (Maria - CIV)");
        log.info("   DOCENTE → jperez@nur.edu.bo / docente123    (Juan - MED)");
        log.info("   ALUMNO  → {CI}@nur.edu.bo / 1234");
        log.info("   Ejemplo Estudiante (en riesgo): Ana Condori - CI 23456789");
        log.info("============================================");
    }

    private boolean contains(int[] arr, int value) {
        for (int i : arr) {
            if (i == value) return true;
        }
        return false;
    }
}
