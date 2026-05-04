package com.universidad_nur.notasnurv3_api.services;

import java.util.Optional;

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

import com.universidad_nur.notasnurv3_api.dto.DegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.DegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.Degree;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.DegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

@ExtendWith(MockitoExtension.class)
class DegreeServiceTest {

    @Mock
    private DegreeRepository degreeRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @InjectMocks
    private DegreeService degreeService;

    private Faculty mockFaculty;
    private Degree mockDegree;

    @BeforeEach
    void setUp() {
        mockFaculty = Faculty.builder()
                .id(1)
                .name("Facultad de Tecnología")
                .code("FT")
                .build();

        mockDegree = Degree.builder()
                .id(1)
                .name("Ingeniería de Sistemas")
                .code("IS")
                .faculty(mockFaculty)
                .build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_throwsResourceNotFoundException_whenFacultyDoesNotExist() {
        DegreeRequest request = new DegreeRequest("Ingeniería de Sistemas", "IS", 99);
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> degreeService.create(request));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void create_throwsDuplicateResourceException_whenCodeAlreadyExists() {
        DegreeRequest request = new DegreeRequest("Otra Carrera", "IS", 1);
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(degreeRepository.existsByCode("IS")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> degreeService.create(request));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameAlreadyExists() {
        DegreeRequest request = new DegreeRequest("Ingeniería de Sistemas", "NW", 1);
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(degreeRepository.existsByCode("NW")).thenReturn(false);
        when(degreeRepository.existsByName("Ingeniería de Sistemas")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> degreeService.create(request));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void create_returnsResponse_whenDataIsValid() {
        DegreeRequest request = new DegreeRequest("Ingeniería de Sistemas", "IS", 1);
        Degree saved = Degree.builder().id(1).name("Ingeniería de Sistemas").code("IS").faculty(mockFaculty).build();

        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(degreeRepository.existsByCode("IS")).thenReturn(false);
        when(degreeRepository.existsByName("Ingeniería de Sistemas")).thenReturn(false);
        when(degreeRepository.save(any(Degree.class))).thenReturn(saved);

        DegreeResponse result = degreeService.create(request);

        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("Ingeniería de Sistemas", result.name());
        assertEquals("IS", result.code());
        assertEquals(1, result.facultyId());
        assertEquals("Facultad de Tecnología", result.facultyName());
        verify(degreeRepository, times(1)).save(any(Degree.class));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_throwsResourceNotFoundException_whenDegreeDoesNotExist() {
        when(degreeRepository.findByIdWithFaculty(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> degreeService.update(99, new DegreeRequest("X", "X", 1)));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void update_throwsResourceNotFoundException_whenNewFacultyDoesNotExist() {
        DegreeRequest request = new DegreeRequest("Ingeniería de Sistemas", "IS", 99);
        when(degreeRepository.findByIdWithFaculty(1)).thenReturn(Optional.of(mockDegree));
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> degreeService.update(1, request));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void update_throwsDuplicateResourceException_whenCodeBelongsToAnotherDegree() {
        DegreeRequest request = new DegreeRequest("Ingeniería de Sistemas", "OTHER", 1);
        when(degreeRepository.findByIdWithFaculty(1)).thenReturn(Optional.of(mockDegree));
        when(degreeRepository.existsByCodeAndIdNot("OTHER", 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> degreeService.update(1, request));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void update_throwsDuplicateResourceException_whenNameBelongsToAnotherDegree() {
        DegreeRequest request = new DegreeRequest("Otra Carrera", "IS", 1);
        when(degreeRepository.findByIdWithFaculty(1)).thenReturn(Optional.of(mockDegree));
        when(degreeRepository.existsByNameAndIdNot("Otra Carrera", 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> degreeService.update(1, request));
        verify(degreeRepository, never()).save(any());
    }

    @Test
    void update_returnsResponse_whenDataIsValid() {
        DegreeRequest request = new DegreeRequest("Ingeniería de Sistemas", "IS", 1);
        when(degreeRepository.findByIdWithFaculty(1)).thenReturn(Optional.of(mockDegree));
        when(degreeRepository.save(any(Degree.class))).thenAnswer(inv -> inv.getArgument(0));

        DegreeResponse result = degreeService.update(1, request);

        assertNotNull(result);
        assertEquals("Ingeniería de Sistemas", result.name());
        assertEquals("IS", result.code());
        verify(degreeRepository, times(1)).save(mockDegree);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_throwsResourceNotFoundException_whenDegreeDoesNotExist() {
        when(degreeRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> degreeService.delete(99));
        verify(degreeRepository, never()).delete(any());
    }

    @Test
    void delete_throwsInvalidOperationException_whenDegreeHasEnrollments() {
        when(degreeRepository.findById(1)).thenReturn(Optional.of(mockDegree));
        when(userDegreeRepository.countByDegree_Id(1)).thenReturn(5L);

        assertThrows(InvalidOperationException.class, () -> degreeService.delete(1));
        verify(degreeRepository, never()).delete(any());
    }

    @Test
    void delete_deletesDegree_whenNoEnrollmentsAssociated() {
        when(degreeRepository.findById(1)).thenReturn(Optional.of(mockDegree));
        when(userDegreeRepository.countByDegree_Id(1)).thenReturn(0L);

        degreeService.delete(1);

        verify(degreeRepository, times(1)).delete(mockDegree);
    }
}
