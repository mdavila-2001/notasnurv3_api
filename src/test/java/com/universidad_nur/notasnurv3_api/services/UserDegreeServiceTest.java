package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Degree;
import com.universidad_nur.notasnurv3_api.entities.ProfileType;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

@ExtendWith(MockitoExtension.class)
class UserDegreeServiceTest {

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @InjectMocks
    private UserDegreeService userDegreeService;

    @Test
    void getByUserId_shouldReturnMappedResponses() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).name("Juan").lastName("Pérez").build();
        Degree degree = Degree.builder().id(1).name("Ingeniería de Sistemas").build();
        UserDegree userDegree = UserDegree.builder()
                .id(10)
                .user(user)
                .degree(degree)
                .status(AcademicStatus.ACTIVE)
                .type(ProfileType.STUDENT)
                .build();

        when(userDegreeRepository.findByUser_Id(userId)).thenReturn(List.of(userDegree));

        // Act
        List<UserDegreeResponse> result = userDegreeService.getByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getId());
        assertEquals("Ingeniería de Sistemas", result.get(0).getDegreeName());
        assertEquals("ACTIVE", result.get(0).getStatus());
        verify(userDegreeRepository, times(1)).findByUser_Id(userId);
    }

    @Test
    void getByUserId_shouldReturnEmptyList_WhenNoRecordsFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userDegreeRepository.findByUser_Id(userId)).thenReturn(List.of());

        // Act
        List<UserDegreeResponse> result = userDegreeService.getByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void openRecord_shouldThrowNotImplemented() {
        assertThrows(ResponseStatusException.class, () -> userDegreeService.openRecord(null));
    }
}
