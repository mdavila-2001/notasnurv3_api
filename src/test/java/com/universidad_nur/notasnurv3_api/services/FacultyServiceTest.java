package com.universidad_nur.notasnurv3_api.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.universidad_nur.notasnurv3_api.dto.FacultyRequest;
import com.universidad_nur.notasnurv3_api.dto.FacultyResponse;
import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.DegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private DegreeRepository degreeRepository;

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @InjectMocks
    private FacultyService facultyService;

    private Faculty mockFaculty;

    @BeforeEach
    void setUp() {
        mockFaculty = Faculty.builder()
                .id(1)
                .name("Facultad de Tecnología")
                .code("FT")
                .build();
    }

    // ── getStats ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnStats_WhenFacultyExists() {
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(userDegreeRepository.countByDegree_Faculty_IdAndStatus(1, AcademicStatus.ACTIVE)).thenReturn(100L);

        FacultyStatsResponse result = facultyService.getStats(1);

        assertNotNull(result);
        assertEquals(1, result.getFacultyId());
        assertEquals("Facultad de Tecnología", result.getFacultyName());
        assertEquals(100L, result.getActiveStudentsCount());
        verify(facultyRepository, times(1)).findById(1);
        verify(userDegreeRepository, times(1)).countByDegree_Faculty_IdAndStatus(1, AcademicStatus.ACTIVE);
    }

    @Test
    void shouldThrowException_WhenFacultyDoesNotExist() {
        when(facultyRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> facultyService.getStats(2));
        verify(facultyRepository, times(1)).findById(2);
        verify(userDegreeRepository, never()).countByDegree_Faculty_IdAndStatus(anyInt(), any());
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_throwsDuplicateResourceException_whenCodeAlreadyExists() {
        FacultyRequest request = new FacultyRequest("Nueva Facultad", "FT");
        when(facultyRepository.existsByCode("FT")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> facultyService.create(request));
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameAlreadyExists() {
        FacultyRequest request = new FacultyRequest("Facultad de Tecnología", "NF");
        when(facultyRepository.existsByCode("NF")).thenReturn(false);
        when(facultyRepository.existsByName("Facultad de Tecnología")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> facultyService.create(request));
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void create_returnsResponse_whenDataIsValid() {
        FacultyRequest request = new FacultyRequest("Facultad de Ingeniería", "FI");
        Faculty saved = Faculty.builder().id(2).name("Facultad de Ingeniería").code("FI").build();

        when(facultyRepository.existsByCode("FI")).thenReturn(false);
        when(facultyRepository.existsByName("Facultad de Ingeniería")).thenReturn(false);
        when(facultyRepository.save(any(Faculty.class))).thenReturn(saved);

        FacultyResponse result = facultyService.create(request);

        assertNotNull(result);
        assertEquals(2, result.id());
        assertEquals("Facultad de Ingeniería", result.name());
        assertEquals("FI", result.code());
        verify(facultyRepository, times(1)).save(any(Faculty.class));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_throwsResourceNotFoundException_whenFacultyDoesNotExist() {
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> facultyService.update(99, new FacultyRequest("X", "X")));
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void update_throwsDuplicateResourceException_whenCodeBelongsToAnotherFaculty() {
        FacultyRequest request = new FacultyRequest("Facultad de Tecnología", "OTHER");
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(facultyRepository.existsByCodeAndIdNot("OTHER", 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> facultyService.update(1, request));
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void update_throwsDuplicateResourceException_whenNameBelongsToAnotherFaculty() {
        FacultyRequest request = new FacultyRequest("Otra Facultad", "FT");
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(facultyRepository.existsByNameAndIdNot("Otra Facultad", 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> facultyService.update(1, request));
        verify(facultyRepository, never()).save(any());
    }

    @Test
    void update_returnsResponse_whenDataIsValid() {
        FacultyRequest request = new FacultyRequest("Facultad de Tecnología", "FT");
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(facultyRepository.save(any(Faculty.class))).thenAnswer(inv -> inv.getArgument(0));

        FacultyResponse result = facultyService.update(1, request);

        assertNotNull(result);
        assertEquals("Facultad de Tecnología", result.name());
        assertEquals("FT", result.code());
        verify(facultyRepository, times(1)).save(mockFaculty);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_throwsResourceNotFoundException_whenFacultyDoesNotExist() {
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> facultyService.delete(99));
        verify(facultyRepository, never()).delete(any());
    }

    @Test
    void delete_throwsInvalidOperationException_whenFacultyHasDegrees() {
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(degreeRepository.countByFacultyId(1)).thenReturn(3L);

        assertThrows(InvalidOperationException.class, () -> facultyService.delete(1));
        verify(facultyRepository, never()).delete(any());
    }

    @Test
    void delete_deletesFaculty_whenNoDegreeAssociated() {
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(degreeRepository.countByFacultyId(1)).thenReturn(0L);

        facultyService.delete(1);

        verify(facultyRepository, times(1)).delete(mockFaculty);
    }
}