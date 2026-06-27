package com.universidad_nur.notasnurv3_api.services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.dto.KardexResponse;
import com.universidad_nur.notasnurv3_api.dto.MySubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.dto.StudentResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Degree;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Users student;
    private Users teacher;
    private Users admin;
    private Degree degree;
    private UserDegree academicRecord;
    private Subject subject;
    private Enrollment enrollment;
    private EnrollmentRequest request;

    @BeforeEach
    void setUp() {
        student = Users.builder()
                .id(UUID.randomUUID())
                .name("Juan")
                .lastName("Perez")
                .ci("1234567")
                .email("juan@nur.edu.bo")
                .role(Role.STUDENT)
                .build();

        teacher = Users.builder()
                .id(UUID.randomUUID())
                .name("Carlos")
                .lastName("Mendoza")
                .ci("7654321")
                .email("carlos@nur.edu.bo")
                .role(Role.TEACHER)
                .build();

        admin = Users.builder()
                .id(UUID.randomUUID())
                .name("Administrador")
                .ci("111111")
                .email("admin@nur.edu.bo")
                .role(Role.ADMIN)
                .build();

        degree = Degree.builder()
                .id(1)
                .name("Ingenieria de Sistemas")
                .build();

        academicRecord = UserDegree.builder()
                .id(10)
                .user(student)
                .degree(degree)
                .status(AcademicStatus.ACTIVE)
                .build();

        subject = Subject.builder()
                .id(100)
                .code("INF-310")
                .name("Taller de Programación V")
                .capacity(30)
                .recordStatus(RecordStatus.ACTIVE)
                .teacher(teacher)
                .build();

        enrollment = Enrollment.builder()
                .id(UUID.randomUUID())
                .academicRecord(academicRecord)
                .subject(subject)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        request = new EnrollmentRequest(10, 100);
    }

    @Test
    void enrollStudent_Success() {
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.existsByAcademicRecordIdAndSubjectId(10, 100)).thenReturn(false);
        
        Enrollment savedEnrollment = Enrollment.builder()
                .id(UUID.randomUUID())
                .academicRecord(academicRecord)
                .subject(subject)
                .build();
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);

        EnrollmentResponse response = enrollmentService.enrollStudent(request);

        assertNotNull(response);
        assertEquals("Juan Perez", response.getStudentName());
        assertEquals("1234567", response.getStudentCi());
        assertEquals("INF-310", response.getSubjectCode());
        assertEquals(29, subject.getCapacity());

        verify(subjectRepository, times(1)).saveAndFlush(subject);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_AcademicRecordNotFound() {
        when(userDegreeRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_AcademicRecordInactive() {
        academicRecord.setStatus(AcademicStatus.INACTIVE);
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));

        assertThrows(InvalidOperationException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_SubjectNotFound() {
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));
        when(subjectRepository.findById(100)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_SubjectInDraftState() {
        subject.setRecordStatus(RecordStatus.DRAFT);
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));

        assertThrows(InvalidOperationException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_SubjectNoCapacity() {
        subject.setCapacity(0);
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));

        assertThrows(InvalidOperationException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_DuplicateEnrollment() {
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.existsByAcademicRecordIdAndSubjectId(10, 100)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_ConcurrencyFailure() {
        when(userDegreeRepository.findById(10)).thenReturn(Optional.of(academicRecord));
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.existsByAcademicRecordIdAndSubjectId(10, 100)).thenReturn(false);
        when(subjectRepository.saveAndFlush(subject)).thenThrow(new OptimisticLockingFailureException("concurrency"));

        assertThrows(OptimisticLockingFailureException.class, () -> enrollmentService.enrollStudent(request));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void withdrawStudent_Success() {
        UUID enrollmentId = enrollment.getId();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        enrollmentService.withdrawStudent(enrollmentId);

        assertEquals(EnrollmentStatus.WITHDRAWN, enrollment.getStatus());
        assertEquals(31, subject.getCapacity());
        verify(subjectRepository, times(1)).saveAndFlush(subject);
        verify(enrollmentRepository, times(1)).save(enrollment);
    }

    @Test
    void withdrawStudent_AlreadyWithdrawn() {
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        UUID enrollmentId = enrollment.getId();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        enrollmentService.withdrawStudent(enrollmentId);

        verify(subjectRepository, never()).saveAndFlush(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void withdrawStudent_NotActive() {
        enrollment.setStatus(EnrollmentStatus.FAILED);
        UUID enrollmentId = enrollment.getId();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        assertThrows(InvalidOperationException.class, () -> enrollmentService.withdrawStudent(enrollmentId));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void withdrawStudent_NotFound() {
        UUID id = UUID.randomUUID();
        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.withdrawStudent(id));
    }

    @Test
    void getStudentsBySubject_AdminUser_Success() {
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectId(100)).thenReturn(List.of(enrollment));

        List<StudentResponseDTO> result = enrollmentService.getStudentsBySubject(100, admin);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan Perez", result.get(0).getFullName());
        assertEquals("Ingenieria de Sistemas", result.get(0).getDegreeName());
    }

    @Test
    void getStudentsBySubject_TeacherUser_Success() {
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectId(100)).thenReturn(List.of(enrollment));

        List<StudentResponseDTO> result = enrollmentService.getStudentsBySubject(100, teacher);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getStudentsBySubject_UnauthorizedUser() {
        Users otherTeacher = Users.builder().id(UUID.randomUUID()).role(Role.TEACHER).build();
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));

        assertThrows(UnauthorizedAccessException.class, () -> enrollmentService.getStudentsBySubject(100, otherTeacher));
    }

    @Test
    void getMySubjects_Success() {
        when(enrollmentRepository.findByAcademicRecord_UserId(student.getId())).thenReturn(List.of(enrollment));

        List<MySubjectResponseDTO> result = enrollmentService.getMySubjects(student);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Taller de Programación V", result.get(0).getSubjectName());
        assertEquals("Carlos Mendoza", result.get(0).getTeacherName());
        assertEquals("Ingenieria de Sistemas", result.get(0).getDegreeName());
    }

    @Test
    void getMyKardex_Success() {
        Management management = Management.builder().year(2025).build();
        Semester semester = Semester.builder().management(management).number(1).build();
        subject.setSemester(semester);

        enrollment.setFinalScore(90);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByAcademicRecord_UserId(student.getId())).thenReturn(List.of(enrollment));

        KardexResponse response = enrollmentService.getMyKardex(student.getEmail());

        assertNotNull(response);
        assertEquals("Juan Perez", response.getStudentName());
        assertEquals("Ingenieria de Sistemas", response.getDegreeName());
        assertTrue(response.getHistoryBySemester().containsKey("Gestión 2025 - Semestre 1"));
    }

    @Test
    void getMyKardex_NoEnrollments() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByAcademicRecord_UserId(student.getId())).thenReturn(Collections.emptyList());

        KardexResponse response = enrollmentService.getMyKardex(student.getEmail());

        assertNotNull(response);
        assertEquals("Juan Perez", response.getStudentName());
        assertTrue(response.getHistoryBySemester().isEmpty());
    }

    @Test
    void getMyKardex_StudentNotFound() {
        when(userRepository.findByEmail("nonexistent@nur.edu.bo")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.getMyKardex("nonexistent@nur.edu.bo"));
    }
}
