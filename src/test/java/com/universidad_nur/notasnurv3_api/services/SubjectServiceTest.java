package com.universidad_nur.notasnurv3_api.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.universidad_nur.notasnurv3_api.config.SecurityAuthorities;
import com.universidad_nur.notasnurv3_api.dto.SubjectResponse;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import com.universidad_nur.notasnurv3_api.repositories.SemesterRepository;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private GradingService gradingService;

    @InjectMocks
    private SubjectService subjectService;

    @Mock
    private SubjectService self;

    private Subject subject;
    private Users teacher;
    private Semester semester;
    private Management management;

    @BeforeEach
    void setUp() {
        teacher = new Users();
        teacher.setId(UUID.randomUUID());
        teacher.setEmail("teacher@nur.edu");
        teacher.setName("Juan");
        teacher.setLastName("Perez");

        management = new Management();
        management.setYear(2026);

        semester = new Semester();
        semester.setId(1);
        semester.setNumber(1);
        semester.setManagement(management);

        subject = new Subject();
        subject.setId(1);
        subject.setCode("INF-301");
        subject.setName("Programacion III");
        subject.setTeacher(teacher);
        subject.setSemester(semester);
        subject.setRecordStatus(RecordStatus.ACTIVE);
    }

    @Test
    void closeSubjectByUser_exito_comoAdministrador() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(SecurityAuthorities.ROLE_ADMIN));
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));
        
        SubjectResponse expectedResponse = SubjectResponse.builder()
                .id(1)
                .code("INF-301")
                .name("Programacion III")
                .recordStatus(RecordStatus.CLOSED)
                .semesterId(1)
                .semesterName("Semestre 1")
                .teacherId(teacher.getId())
                .teacherName("Juan Perez")
                .management("2026")
                .build();

        try {
            java.lang.reflect.Field selfField = SubjectService.class.getDeclaredField("self");
            selfField.setAccessible(true);
            selfField.set(subjectService, self);
        } catch (Exception e) {
        }

        when(self.closeSubject(1)).thenReturn(expectedResponse);

        SubjectResponse response = subjectService.closeSubjectByUser(1, "another_admin@nur.edu", authorities);

        assertNotNull(response);
        assertEquals(RecordStatus.CLOSED, response.getRecordStatus());
        verify(self).closeSubject(1);
    }

    @Test
    void closeSubjectByUser_exito_comoDocentePropietario() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(SecurityAuthorities.ROLE_TEACHER));
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));

        SubjectResponse expectedResponse = SubjectResponse.builder()
                .id(1)
                .code("INF-301")
                .name("Programacion III")
                .recordStatus(RecordStatus.CLOSED)
                .semesterId(1)
                .semesterName("Semestre 1")
                .teacherId(teacher.getId())
                .teacherName("Juan Perez")
                .management("2026")
                .build();

        try {
            java.lang.reflect.Field selfField = SubjectService.class.getDeclaredField("self");
            selfField.setAccessible(true);
            selfField.set(subjectService, self);
        } catch (Exception e) {
        }

        when(self.closeSubject(1)).thenReturn(expectedResponse);

        SubjectResponse response = subjectService.closeSubjectByUser(1, "teacher@nur.edu", authorities);

        assertNotNull(response);
        assertEquals(RecordStatus.CLOSED, response.getRecordStatus());
        verify(self).closeSubject(1);
    }

    @Test
    void closeSubjectByUser_lanzaUnauthorizedAccessException_comoDocenteNoPropietario() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(SecurityAuthorities.ROLE_TEACHER));
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));

        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> subjectService.closeSubjectByUser(1, "wrong_teacher@nur.edu", authorities)
        );

        assertEquals("No tienes permisos para cerrar el acta de esta materia.", exception.getMessage());
    }

    @Test
    void closeSubjectByUser_lanzaResourceNotFoundException_cuandoMateriaNoExiste() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(SecurityAuthorities.ROLE_ADMIN));
        when(subjectRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subjectService.closeSubjectByUser(999, "admin@nur.edu", authorities)
        );
    }

    @Test
    void updateSubject_exito() {
        com.universidad_nur.notasnurv3_api.dto.SubjectRequest request = new com.universidad_nur.notasnurv3_api.dto.SubjectRequest();
        request.setName("Programacion Avanzada");
        request.setCapacity(40);
        request.setTeacherId(teacher.getId());
        request.setSemesterId(semester.getId());
        request.setRecordStatus(RecordStatus.ACTIVE);

        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(semesterRepository.findById(semester.getId())).thenReturn(Optional.of(semester));
        
        teacher.setRole(Role.TEACHER);
        
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubjectResponse response = subjectService.updateSubject(1, request);

        assertNotNull(response);
        assertEquals("Programacion Avanzada", response.getName());
        assertEquals(40, response.getCapacity());
        assertEquals(teacher.getId(), response.getTeacherId());
        assertEquals(semester.getId(), response.getSemesterId());
        assertEquals(RecordStatus.ACTIVE, response.getRecordStatus());
    }

    @Test
    void updateSubject_docenteNoExiste() {
        UUID nonExistentTeacherId = UUID.randomUUID();
        com.universidad_nur.notasnurv3_api.dto.SubjectRequest request = new com.universidad_nur.notasnurv3_api.dto.SubjectRequest();
        request.setName("Programacion Avanzada");
        request.setTeacherId(nonExistentTeacherId);

        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));
        when(userRepository.findById(nonExistentTeacherId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subjectService.updateSubject(1, request)
        );
    }

    @Test
    void updateSubject_usuarioNoEsDocente() {
        com.universidad_nur.notasnurv3_api.dto.SubjectRequest request = new com.universidad_nur.notasnurv3_api.dto.SubjectRequest();
        request.setName("Programacion Avanzada");
        request.setTeacherId(teacher.getId());

        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        
        teacher.setRole(Role.STUDENT);

        assertThrows(
                InvalidOperationException.class,
                () -> subjectService.updateSubject(1, request)
        );
    }
}
