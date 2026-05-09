package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.SemesterRequest;
import com.universidad_nur.notasnurv3_api.dto.SemesterResponse;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidDateRangeException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import com.universidad_nur.notasnurv3_api.repositories.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private ManagementRepository managementRepository;

    @InjectMocks
    private SemesterService semesterService;

    @Test
    void update_lanzaRuntimeException_cuandoNumeroNoEs1Ni2() {
        Integer id = 1;
        Semester semester = semesterBase(id, 1, 2026, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(3, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> semesterService.update(id, request));

        assertEquals("El número de semestre debe ser 1 o 2.", exception.getMessage());
        verify(semesterRepository, never()).save(any(Semester.class));
    }

    @Test
    void update_lanzaInvalidDateRangeException_cuandoEndDateNoEsPosteriorAStartDate() {
        Integer id = 1;
        Semester semester = semesterBase(id, 1, 2026, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));

        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> semesterService.update(id, request)
        );

        assertEquals("La fecha de fin debe ser posterior a la fecha de inicio.", exception.getMessage());
        verify(semesterRepository, never()).save(any(Semester.class));
    }

    @Test
    void update_lanzaInvalidDateRangeException_cuandoStartDateEsAnteriorAlPrimeroDeEnero() {
        Integer id = 1;
        Semester semester = semesterBase(id, 1, 2026, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(1, LocalDate.of(2025, 12, 31), LocalDate.of(2026, 6, 10), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));

        assertThrows(InvalidDateRangeException.class, () -> semesterService.update(id, request));
        verify(semesterRepository, never()).save(any(Semester.class));
    }

    @Test
    void update_lanzaInvalidDateRangeException_cuandoEndDateEsPosteriorAlTreintaYUnoDeDiciembre() {
        Integer id = 1;
        Semester semester = semesterBase(id, 1, 2026, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(1, LocalDate.of(2026, 7, 1), LocalDate.of(2027, 1, 1), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));

        assertThrows(InvalidDateRangeException.class, () -> semesterService.update(id, request));
        verify(semesterRepository, never()).save(any(Semester.class));
    }

    @Test
    void update_lanzaDuplicateResourceException_cuandoCambiaNumeroADuplicado() {
        Integer id = 1;
        Management management = management(10, 2026);
        Semester semester = semesterBase(id, 1, management, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 20), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));
        when(semesterRepository.existsByManagementAndNumber(management, 2)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> semesterService.update(id, request));
        verify(semesterRepository, never()).save(any(Semester.class));
    }

    @Test
    void update_actualizaCorrectamente_cuandoDatosValidosYNumeroNoCambia() {
        Integer id = 1;
        Management management = management(10, 2026);
        Semester semester = semesterBase(id, 1, management, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));
        when(semesterRepository.save(any(Semester.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SemesterResponse response = semesterService.update(id, request);

        assertEquals(1, response.number());
        assertEquals(LocalDate.of(2026, 2, 1), response.startDate());
        assertEquals(LocalDate.of(2026, 6, 30), response.endDate());
        assertEquals(10, response.managementId());
        assertEquals(2026, response.managementYear());
        verify(semesterRepository, never()).existsByManagementAndNumber(any(Management.class), any(Integer.class));
        verify(semesterRepository).save(semester);
    }

    @Test
    void update_actualizaCorrectamente_cuandoNumeroCambiaYNoHayDuplicado() {
        Integer id = 1;
        Management management = management(10, 2026);
        Semester semester = semesterBase(id, 1, management, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10));
        SemesterRequest request = request(2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 15), 10);

        when(semesterRepository.findById(id)).thenReturn(Optional.of(semester));
        when(semesterRepository.existsByManagementAndNumber(management, 2)).thenReturn(false);
        when(semesterRepository.save(any(Semester.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SemesterResponse response = semesterService.update(id, request);

        assertEquals(2, response.number());
        assertEquals(LocalDate.of(2026, 7, 1), response.startDate());
        assertEquals(LocalDate.of(2026, 12, 15), response.endDate());
        verify(semesterRepository).existsByManagementAndNumber(management, 2);
        verify(semesterRepository).save(semester);
    }

    @Test
    void delete_eliminaCorrectamente() {
        Integer id = 7;
        when(semesterRepository.existsById(id)).thenReturn(true);

        semesterService.delete(id);

        verify(semesterRepository).deleteById(id);
    }

    @Test
    void getById_lanzaResourceNotFoundException_cuandoIdNoExiste() {
        Integer id = 99;
        when(semesterRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> semesterService.getById(id)
        );

        assertEquals("Semestre no encontrado con id: " + id, exception.getMessage());
    }

    private SemesterRequest request(Integer number, LocalDate startDate, LocalDate endDate, Integer managementId) {
        return new SemesterRequest(number, startDate, endDate, managementId);
    }

    private Management management(Integer id, Integer year) {
        Management management = new Management();
        management.setId(id);
        management.setYear(year);
        return management;
    }

    private Semester semesterBase(Integer id, Integer number, Integer year, LocalDate startDate, LocalDate endDate) {
        return semesterBase(id, number, management(10, year), startDate, endDate);
    }

    private Semester semesterBase(Integer id, Integer number, Management management, LocalDate startDate, LocalDate endDate) {
        Semester semester = new Semester();
        semester.setId(id);
        semester.setNumber(number);
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);
        semester.setManagement(management);
        return semester;
    }
}
