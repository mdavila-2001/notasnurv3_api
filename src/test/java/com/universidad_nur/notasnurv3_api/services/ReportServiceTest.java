package com.universidad_nur.notasnurv3_api.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private ReportService reportService;

    private Subject subject;
    private Enrollment enrollment;
    private Users student;
    private UserDegree academicRecord;

    @BeforeEach
    void setUp() {
        Management management = Management.builder().year(2025).build();
        Semester semester = Semester.builder().management(management).number(1).build();

        Users teacher = Users.builder().name("Carlos").lastName("Mendoza").build();

        subject = Subject.builder()
                .id(100)
                .code("INF-310")
                .name("Taller de Programación V")
                .recordStatus(RecordStatus.ACTIVE)
                .semester(semester)
                .teacher(teacher)
                .build();

        student = Users.builder()
                .name("Juan")
                .lastName("Perez")
                .ci("1234567")
                .build();

        academicRecord = UserDegree.builder()
                .user(student)
                .build();

        enrollment = Enrollment.builder()
                .id(UUID.randomUUID())
                .academicRecord(academicRecord)
                .subject(subject)
                .status(EnrollmentStatus.ACTIVE)
                .finalScore(85)
                .build();
    }

    @Test
    void generateActaNotasPdf_Success() {
        when(subjectRepository.findById(100)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectId(100)).thenReturn(List.of(enrollment));

        byte[] pdfBytes = reportService.generateActaNotasPdf(100);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateActaNotasPdf_SubjectNotFound() {
        when(subjectRepository.findById(100)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.generateActaNotasPdf(100));
    }

    @Test
    void generateAsistenciaExcel_Success() {
        when(subjectRepository.existsById(100)).thenReturn(true);
        when(enrollmentRepository.findBySubjectId(100)).thenReturn(List.of(enrollment));

        UUID enrollmentId = enrollment.getId();
        Object[] row = new Object[]{enrollmentId, 2L};
        when(attendanceRepository.countByEnrollmentIdsAndStatus(List.of(enrollmentId), AttendanceStatus.ABSENT))
                .thenReturn(java.util.Collections.singletonList(row));

        byte[] excelBytes = reportService.generateAsistenciaExcel(100);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    void generateAsistenciaExcel_SubjectNotFound() {
        when(subjectRepository.existsById(100)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reportService.generateAsistenciaExcel(100));
    }

    @Test
    void generateAsistenciaExcel_NoEnrollments() {
        when(subjectRepository.existsById(100)).thenReturn(true);
        when(enrollmentRepository.findBySubjectId(100)).thenReturn(Collections.emptyList());

        byte[] excelBytes = reportService.generateAsistenciaExcel(100);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }
}
