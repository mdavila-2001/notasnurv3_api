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

import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @InjectMocks
    private FacultyService facultyService;

    private Faculty mockFaculty;

    @BeforeEach
    void setUp() {
        // Usamos Builder que es como viene en dev
        mockFaculty = Faculty.builder()
                .id(1)
                .name("Facultad de Tecnología")
                .build();
    }

    @Test
    void shouldReturnStats_WhenFacultyExists() {
        // Arrange
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(userDegreeRepository.countByDegree_Faculty_IdAndStatus(1, AcademicStatus.ACTIVE)).thenReturn(100L);

        // Act
        FacultyStatsResponse result = facultyService.getStats(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getFacultyId()); // Validamos el ID que se agregó en dev
        assertEquals("Facultad de Tecnología", result.getFacultyName());
        assertEquals(100L, result.getActiveStudentsCount());
        
        verify(facultyRepository, times(1)).findById(1);
        verify(userDegreeRepository, times(1)).countByDegree_Faculty_IdAndStatus(1, AcademicStatus.ACTIVE);
    }

    @Test
    void shouldThrowException_WhenFacultyDoesNotExist() {
        // Arrange
        when(facultyRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> facultyService.getStats(2));
        
        verify(facultyRepository, times(1)).findById(2);
        verify(userDegreeRepository, never()).countByDegree_Faculty_IdAndStatus(anyInt(), any());
    }
}