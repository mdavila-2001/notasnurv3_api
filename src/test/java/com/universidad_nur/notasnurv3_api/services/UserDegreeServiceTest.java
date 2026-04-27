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
class UserDegreeServiceTest {

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private UserDegreeService userDegreeService;

    private Faculty mockFaculty;

    @BeforeEach
    void setUp() {
        mockFaculty = new Faculty();
        mockFaculty.setId(1);
        mockFaculty.setName("Facultad de Ingeniería");
    }

    @Test
    void shouldReturnCorrectStats_WhenFacultyExists() {
        // Arrange
        when(facultyRepository.findById(1)).thenReturn(Optional.of(mockFaculty));
        when(userDegreeRepository.countByDegree_Faculty_IdAndStatus(1, AcademicStatus.ACTIVE)).thenReturn(150L);

        // Act
        FacultyStatsResponse result = userDegreeService.getFacultyStats(1);

        // Assert
        assertNotNull(result);
        assertEquals("Facultad de Ingeniería", result.getFacultyName());
        assertEquals(150L, result.getActiveStudentsCount());
        verify(userDegreeRepository, times(1)).countByDegree_Faculty_IdAndStatus(1, AcademicStatus.ACTIVE);
    }

    @Test
    void shouldThrowException_WhenFacultyDoesNotExist() {
        // Arrange
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userDegreeService.getFacultyStats(99));
        verify(userDegreeRepository, never()).countByDegree_Faculty_IdAndStatus(anyInt(), any());
    }
}
