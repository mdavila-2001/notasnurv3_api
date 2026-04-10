package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.ComponentRequest;
import com.universidad_nur.notasnurv3_api.dto.ComponentResponse;
import com.universidad_nur.notasnurv3_api.services.ComponentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {
    private final ComponentService componentService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<ComponentResponse>> create(@Valid @RequestBody ComponentRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Componente registrado", componentService.addComponent(request)));
    }
}
