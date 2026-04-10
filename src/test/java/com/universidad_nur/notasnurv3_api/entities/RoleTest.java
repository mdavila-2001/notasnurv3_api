package com.universidad_nur.notasnurv3_api.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void authorityMappingShouldMatchExpectedValues() {
        assertEquals("ROLE_ADMIN", Role.ADMIN.authority());
        assertEquals("ROLE_TEACHER", Role.TEACHER.authority());
        assertEquals("ROLE_STUDENT", Role.STUDENT.authority());
    }

    @Test
    void helperFlagsShouldBeConsistent() {
        assertTrue(Role.ADMIN.isAdmin());
        assertTrue(Role.TEACHER.isTeacher());
        assertTrue(Role.STUDENT.isStudent());

        assertFalse(Role.ADMIN.isStudent());
        assertFalse(Role.STUDENT.isTeacher());
        assertFalse(Role.TEACHER.isAdmin());
    }
}

