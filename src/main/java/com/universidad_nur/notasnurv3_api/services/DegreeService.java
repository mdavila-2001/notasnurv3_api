package com.universidad_nur.notasnurv3_api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.universidad_nur.notasnurv3_api.dto.DegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.DegreeResponse;

@Service
public class DegreeService {

    public DegreeResponse create(DegreeRequest request) {
        // Por ahora lo dejamos vacío para que compile
        return null;
    }

    public List<DegreeResponse> getAll() {
        // Por ahora lo dejamos vacío para que compile
        return List.of();
    }
}