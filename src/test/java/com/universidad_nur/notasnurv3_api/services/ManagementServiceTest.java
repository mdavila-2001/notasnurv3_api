package com.universidad_nur.notasnurv3_api.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.Sort;

import com.universidad_nur.notasnurv3_api.dto.ManagementRequest;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponse;
import com.universidad_nur.notasnurv3_api.dto.ManagementStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Modality;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;

@ExtendWith(MockitoExtension.class)
public class ManagementServiceTest {

    @Mock
    private ManagementRepository managementRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private ManagementService managementService;

    private Management management;
    private ManagementRequest request;

    @BeforeEach
    void setUp() {
        management = Management.builder()
                .id(1)
                .year(2025)
                .semesters(Collections.emptyList())
                .build();

        request = new ManagementRequest(2025);
    }

    @Test
    void createManagement_Success() {
        when(managementRepository.existsByYear(2025)).thenReturn(false);
        when(managementRepository.save(any(Management.class))).thenReturn(management);

        ManagementResponse response = managementService.createManagement(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(2025, response.year());
        verify(managementRepository, times(1)).save(any(Management.class));
    }

    @Test
    void createManagement_DuplicateYear() {
        when(managementRepository.existsByYear(2025)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> managementService.createManagement(request));
        verify(managementRepository, never()).save(any());
    }

    @Test
    void getAllManagements_Success() {
        when(managementRepository.findAll(Sort.by(Sort.Direction.ASC, "year"))).thenReturn(List.of(management));

        List<ManagementResponse> result = managementService.getAllManagements();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2025, result.get(0).year());
    }

    @Test
    void getById_Success() {
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));

        ManagementResponse response = managementService.getById(1);

        assertNotNull(response);
        assertEquals(2025, response.year());
    }

    @Test
    void getById_NotFound() {
        when(managementRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> managementService.getById(99));
    }

    @Test
    void update_Success() {
        ManagementRequest updateRequest = new ManagementRequest(2026);
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));
        when(managementRepository.existsByYear(2026)).thenReturn(false);
        when(managementRepository.save(management)).thenReturn(management);

        ManagementResponse response = managementService.update(1, updateRequest);

        assertNotNull(response);
        verify(managementRepository, times(1)).save(management);
    }

    @Test
    void update_DuplicateYear() {
        ManagementRequest updateRequest = new ManagementRequest(2026);
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));
        when(managementRepository.existsByYear(2026)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> managementService.update(1, updateRequest));
        verify(managementRepository, never()).save(any());
    }

    @Test
    void update_SameYear_Success() {
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));
        when(managementRepository.save(management)).thenReturn(management);

        ManagementResponse response = managementService.update(1, request);

        assertNotNull(response);
        verify(managementRepository, times(1)).save(management);
    }

    @Test
    void deleteManagement_Success() {
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));

        managementService.deleteManagement(1);

        verify(managementRepository, times(1)).delete(management);
    }

    @Test
    void deleteManagement_HasSemesters() {
        management.setSemesters(List.of(new Semester()));
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));

        assertThrows(RuntimeException.class, () -> managementService.deleteManagement(1));
        verify(managementRepository, never()).delete(any());
    }

    @Test
    void deleteManagement_NotFound() {
        when(managementRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> managementService.deleteManagement(99));
    }

    @Test
    void getStats_Success() {
        Subject subject = Subject.builder().modality(Modality.FACE_TO_FACE).build();
        Enrollment enrollmentActive = Enrollment.builder()
                .id(UUID.randomUUID())
                .status(EnrollmentStatus.ACTIVE)
                .subject(subject)
                .build();
        Enrollment enrollmentPassed = Enrollment.builder()
                .id(UUID.randomUUID())
                .status(EnrollmentStatus.PASSED)
                .subject(subject)
                .build();
        
        when(managementRepository.findById(1)).thenReturn(Optional.of(management));
        when(enrollmentRepository.findBySubject_Semester_ManagementId(1)).thenReturn(List.of(enrollmentActive, enrollmentPassed));
        
        UUID activeId = enrollmentActive.getId();
        Object[] row = new Object[]{activeId, 4L};
        when(attendanceRepository.countByEnrollmentIdsAndStatus(List.of(activeId), AttendanceStatus.ABSENT))
                .thenReturn(java.util.Collections.singletonList(row));
        when(systemSettingService.getAbsenceLimit(Modality.FACE_TO_FACE)).thenReturn(5);

        ManagementStatsResponse stats = managementService.getStats(1);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalEnrollments());
        assertEquals(1, stats.getPassedEnrollments());
        assertEquals(50.0, stats.getPassRatePercentage());
        assertEquals(1, stats.getStudentsAtRisk());
    }

    @Test
    void getStats_NotFound() {
        when(managementRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> managementService.getStats(99));
    }
}
