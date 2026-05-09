package com.universidad_nur.notasnurv3_api.entities;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsersTest {

    @Test
    void getAuthoritiesShouldReturnRoleAuthority() {
        Users user = Users.builder()
                .role(Role.TEACHER)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_TEACHER", authorities.iterator().next().getAuthority());
    }

    @Test
    void getAuthoritiesShouldBeEmptyWhenRoleIsNull() {
        Users user = Users.builder().build();

        assertTrue(user.getAuthorities().isEmpty());
    }
}

