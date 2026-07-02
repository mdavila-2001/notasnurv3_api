package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.AttendanceBulkRequest;
import com.universidad_nur.notasnurv3_api.dto.StudentAttendance;
import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import com.universidad_nur.notasnurv3_api.repositories.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SystemSettingService systemSettingService;
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Users teacher;
    private Subject subject;
    private Enrollment enrollment1;
    private Enrollment enrollment2;
    private UUID enrollmentId1;
    private UUID enrollmentId2;
    private List<Enrollment> enrollmentList;

    @BeforeEach
    void setUp() {
        teacher = new Users();
        teacher.setId(UUID.randomUUID());
        teacher.setEmail("teacher@nur.edu");
        teacher.setName("Juan");
        teacher.setLastName("Perez");

        subject = new Subject();
        subject.setId(101);
        subject.setTeacher(teacher);
        subject.setRecordStatus(RecordStatus.ACTIVE);
        subject.setModality(Modality.FACE_TO_FACE);

        enrollmentId1 = UUID.randomUUID();
        enrollmentId2 = UUID.randomUUID();

        UserDegree studentRecord1 = new UserDegree();
        Users student1 = new Users();
        student1.setId(UUID.randomUUID());
        student1.setName("Estudiante");
        student1.setLastName("Uno");
        studentRecord1.setUser(student1);

        UserDegree studentRecord2 = new UserDegree();
        Users student2 = new Users();
        student2.setId(UUID.randomUUID());
        student2.setName("Estudiante");
        student2.setLastName("Dos");
        studentRecord2.setUser(student2);

        enrollment1 = Enrollment.builder()
                .id(enrollmentId1)
                .subject(subject)
                .academicRecord(studentRecord1)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        enrollment2 = Enrollment.builder()
                .id(enrollmentId2)
                .subject(subject)
                .academicRecord(studentRecord2)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        enrollmentList = List.of(enrollment1, enrollment2);
    }

    @Test
    void saveBulkAttendance_exito_cuandoDatosSonValidos() {
        LocalDate today = LocalDate.now();
        List<StudentAttendance> records = List.of(
                new StudentAttendance(enrollmentId1, AttendanceStatus.PRESENT),
                new StudentAttendance(enrollmentId2, AttendanceStatus.ABSENT)
        );
        AttendanceBulkRequest request = new AttendanceBulkRequest(101, today, records);

        when(subjectRepository.findById(101)).thenReturn(Optional.of(subject));
        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findBySubjectId(101)).thenReturn(enrollmentList);
        when(attendanceRepository.findByEnrollmentIdInAndDate(anyList(), eq(today))).thenReturn(List.of());

        Attendance attendance1 = Attendance.builder().id(UUID.randomUUID()).enrollment(enrollment1).date(today).status(AttendanceStatus.PRESENT).build();
        Attendance attendance2 = Attendance.builder().id(UUID.randomUUID()).enrollment(enrollment2).date(today).status(AttendanceStatus.ABSENT).build();
        when(attendanceRepository.saveAll(anyList())).thenReturn(List.of(attendance1, attendance2));

        List<Object[]> absencesCounts = new ArrayList<>();
        absencesCounts.add(new Object[]{enrollmentId1, 1L});
        absencesCounts.add(new Object[]{enrollmentId2, 2L});
        when(attendanceRepository.countByEnrollmentIdsAndStatus(anyList(), eq(AttendanceStatus.ABSENT))).thenReturn(absencesCounts);
        when(systemSettingService.getAbsenceLimit(Modality.FACE_TO_FACE)).thenReturn(5);

        attendanceService.saveBulkAttendance(request, "teacher@nur.edu");

        verify(attendanceRepository).saveAll(anyList());
        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
        assertEquals(EnrollmentStatus.ACTIVE, enrollment1.getStatus());
        assertEquals(EnrollmentStatus.ACTIVE, enrollment2.getStatus());
    }

    @Test
    void saveBulkAttendance_lanzaExcepcion_cuandoFechaEsFutura() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        List<StudentAttendance> records = List.of(
                new StudentAttendance(enrollmentId1, AttendanceStatus.PRESENT)
        );
        AttendanceBulkRequest request = new AttendanceBulkRequest(101, futureDate, records);

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> attendanceService.saveBulkAttendance(request, "teacher@nur.edu")
        );
        assertEquals("No se puede registrar asistencia en fechas futuras.", exception.getMessage());
        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void saveBulkAttendance_lanzaExcepcion_cuandoMateriaEstaCerrada() {
        subject.setRecordStatus(RecordStatus.CLOSED);
        LocalDate today = LocalDate.now();
        List<StudentAttendance> records = List.of(
                new StudentAttendance(enrollmentId1, AttendanceStatus.PRESENT)
        );
        AttendanceBulkRequest request = new AttendanceBulkRequest(101, today, records);

        when(subjectRepository.findById(101)).thenReturn(Optional.of(subject));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> attendanceService.saveBulkAttendance(request, "teacher@nur.edu")
        );
        assertEquals("No se puede registrar asistencia si la materia está en estado CLOSED.", exception.getMessage());
    }

    @Test
    void saveBulkAttendance_lanzaExcepcion_cuandoDocenteNoTienePermiso() {
        Users otherTeacher = new Users();
        otherTeacher.setId(UUID.randomUUID());
        otherTeacher.setEmail("other@nur.edu");
        
        subject.setTeacher(otherTeacher);

        LocalDate today = LocalDate.now();
        List<StudentAttendance> records = List.of(
                new StudentAttendance(enrollmentId1, AttendanceStatus.PRESENT)
        );
        AttendanceBulkRequest request = new AttendanceBulkRequest(101, today, records);

        when(subjectRepository.findById(101)).thenReturn(Optional.of(subject));
        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));

        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> attendanceService.saveBulkAttendance(request, "teacher@nur.edu")
        );
        assertEquals("No tienes permisos para registrar asistencia en esta materia.", exception.getMessage());
    }

    @Test
    void saveBulkAttendance_marcaEstudianteComoReprobado_cuandoFaltasSuperanLimite_FACE_TO_FACE() {
        LocalDate today = LocalDate.now();
        List<StudentAttendance> records = List.of(
                new StudentAttendance(enrollmentId1, AttendanceStatus.ABSENT)
        );
        AttendanceBulkRequest request = new AttendanceBulkRequest(101, today, records);

        when(subjectRepository.findById(101)).thenReturn(Optional.of(subject));
        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findBySubjectId(101)).thenReturn(enrollmentList);
        when(attendanceRepository.findByEnrollmentIdInAndDate(anyList(), eq(today))).thenReturn(List.of());

        Attendance attendance1 = Attendance.builder().id(UUID.randomUUID()).enrollment(enrollment1).date(today).status(AttendanceStatus.ABSENT).build();
        when(attendanceRepository.saveAll(anyList())).thenReturn(List.of(attendance1));

        List<Object[]> absencesCounts = new ArrayList<>();
        absencesCounts.add(new Object[]{enrollmentId1, 6L});
        when(attendanceRepository.countByEnrollmentIdsAndStatus(anyList(), eq(AttendanceStatus.ABSENT))).thenReturn(absencesCounts);
        when(systemSettingService.getAbsenceLimit(Modality.FACE_TO_FACE)).thenReturn(5);

        attendanceService.saveBulkAttendance(request, "teacher@nur.edu");

        verify(enrollmentRepository).saveAll(anyList());
        assertEquals(EnrollmentStatus.FAILED_BY_ATTENDANCE, enrollment1.getStatus());
    }

    @Test
    void saveBulkAttendance_marcaEstudianteComoReprobado_cuandoFaltasSuperanLimite_BLENDED() {
        subject.setModality(Modality.BLENDED);
        LocalDate today = LocalDate.now();
        List<StudentAttendance> records = List.of(
                new StudentAttendance(enrollmentId2, AttendanceStatus.ABSENT)
        );
        AttendanceBulkRequest request = new AttendanceBulkRequest(101, today, records);

        when(subjectRepository.findById(101)).thenReturn(Optional.of(subject));
        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findBySubjectId(101)).thenReturn(enrollmentList);
        when(attendanceRepository.findByEnrollmentIdInAndDate(anyList(), eq(today))).thenReturn(List.of());

        Attendance attendance2 = Attendance.builder().id(UUID.randomUUID()).enrollment(enrollment2).date(today).status(AttendanceStatus.ABSENT).build();
        when(attendanceRepository.saveAll(anyList())).thenReturn(List.of(attendance2));

        List<Object[]> absencesCounts = new ArrayList<>();
        absencesCounts.add(new Object[]{enrollmentId2, 4L});
        when(attendanceRepository.countByEnrollmentIdsAndStatus(anyList(), eq(AttendanceStatus.ABSENT))).thenReturn(absencesCounts);
        when(systemSettingService.getAbsenceLimit(Modality.BLENDED)).thenReturn(3);

        attendanceService.saveBulkAttendance(request, "teacher@nur.edu");

        verify(enrollmentRepository).saveAll(anyList());
        assertEquals(EnrollmentStatus.FAILED_BY_ATTENDANCE, enrollment2.getStatus());
    }
}
