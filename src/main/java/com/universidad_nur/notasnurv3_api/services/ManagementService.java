package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.ManagementRequest;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponse;
import com.universidad_nur.notasnurv3_api.dto.ManagementStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagementService {

    private final ManagementRepository managementRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SystemSettingService systemSettingService;

    @Transactional
    public ManagementResponse createManagement(ManagementRequest request) {
        if (managementRepository.existsByYear(request.year())) {
            throw new DuplicateResourceException("Ya existe una gestión registrada para el año " + request.year() + ".");
        }

        Management management = Management.builder()
                .year(request.year())
                .build();

        Management savedManagement = managementRepository.save(management);
        return toResponse(savedManagement);
    }

    @Transactional(readOnly = true)
    public List<ManagementResponse> getAllManagements() {
        return managementRepository.findAll(Sort.by(Sort.Direction.ASC, "year"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ManagementResponse getById(Integer id) {
        return managementRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con id: " + id));
    }

    @Transactional
    public ManagementResponse update(Integer id, ManagementRequest request) {
        Management management = managementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con id: " + id));

        // Validar si el nuevo año ya existe en otra gestión
        if (!management.getYear().equals(request.year()) &&
                managementRepository.existsByYear(request.year())) {
            throw new DuplicateResourceException("El año " + request.year() + " ya está registrado");
        }

        management.setYear(request.year());
        return toResponse(managementRepository.save(management));
    }

    @Transactional
    public void deleteManagement(Integer id) {
        Management management = managementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La gestión no existe."));

        if (management.getSemesters() != null && !management.getSemesters().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la gestión porque tiene semestres asociados.");
        }

        managementRepository.delete(management);
    }

    @Transactional(readOnly = true)
    public ManagementStatsResponse getStats(Integer managementId) {
        Management management = managementRepository.findById(managementId)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada."));

        List<Enrollment> enrollments = enrollmentRepository.findBySubject_Semester_ManagementId(managementId);
        List<Enrollment> activeEnrollments = enrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .toList();
        Map<UUID, Long> absencesByEnrollmentId = countAbsencesByEnrollmentIds(
                activeEnrollments.stream().map(Enrollment::getId).toList()
        );

        long totalEnrollments = enrollments.size();
        long passedEnrollments = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PASSED).count();
        long failedEnrollments = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.FAILED || e.getStatus() == EnrollmentStatus.FAILED_BY_ATTENDANCE).count();

        double passRatePercentage = totalEnrollments > 0 ? ((double) passedEnrollments / totalEnrollments) * 100 : 0.0;

        // Calcular alumnos en riesgo: aquellos cuyas faltas actuales están exactamente a 1 del límite
        long studentsAtRisk = 0;
        for (Enrollment enrollment : activeEnrollments) {
            long absences = absencesByEnrollmentId.getOrDefault(enrollment.getId(), 0L);
            int limit = systemSettingService.getAbsenceLimit(enrollment.getSubject().getModality());

            if (absences == limit - 1) {
                studentsAtRisk++;
            }
        }

        return ManagementStatsResponse.builder()
                .managementId(management.getId())
                .managementYear(String.valueOf(management.getYear()))
                .totalEnrollments(totalEnrollments)
                .passedEnrollments(passedEnrollments)
                .failedEnrollments(failedEnrollments)
                .passRatePercentage(passRatePercentage)
                .studentsAtRisk(studentsAtRisk)
                .build();
    }

    private Map<UUID, Long> countAbsencesByEnrollmentIds(List<UUID> enrollmentIds) {
        if (enrollmentIds.isEmpty()) {
            return Map.of();
        }

        return attendanceRepository.countByEnrollmentIdsAndStatus(enrollmentIds, AttendanceStatus.ABSENT).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private ManagementResponse toResponse(Management management) {
        return new ManagementResponse(management.getId(), management.getYear());
    }
}