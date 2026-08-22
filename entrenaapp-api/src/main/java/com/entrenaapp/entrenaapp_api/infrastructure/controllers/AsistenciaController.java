package com.entrenaapp.entrenaapp_api.infrastructure.controllers;

import com.entrenaapp.entrenaapp_api.application.dto.AsistenciaRequest;
import com.entrenaapp.entrenaapp_api.application.dto.AsistenciaResponse;
import com.entrenaapp.entrenaapp_api.application.services.AsistenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @PostMapping
    public ResponseEntity<AsistenciaResponse> registrar(@Valid @RequestBody AsistenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asistenciaService.registrarAsistencia(request));
    }

    @GetMapping("/entrenamiento/{entrenamientoId}")
    public ResponseEntity<List<AsistenciaResponse>> obtenerPorEntrenamiento(@PathVariable String entrenamientoId) {
        return ResponseEntity.ok(asistenciaService.obtenerPorEntrenamiento(entrenamientoId));
    }
}
