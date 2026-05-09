package com.universidad_nur.notasnurv3_api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.UserRequest;
import com.universidad_nur.notasnurv3_api.dto.UserResponse;
import com.universidad_nur.notasnurv3_api.dto.UserStatusRequest;
import com.universidad_nur.notasnurv3_api.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping("/role/{roleName}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByRole(@PathVariable String roleName) {
        List<UserResponse> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(new ApiResponse<>(true, "Usuarios obtenidos correctamente", users));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody UserRequest user) {
        userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Usuario creado correctamente", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id, @Valid @RequestBody UserRequest user) {
        UserResponse updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Usuario actualizado correctamente", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Usuario eliminado correctamente", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusRequest body) {
        UserResponse updatedUser = userService.updateStatus(id, body.status());
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado actualizado correctamente", updatedUser));
    }
}