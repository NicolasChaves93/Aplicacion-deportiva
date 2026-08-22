package com.entrenaapp.entrenaapp_api.infrastructure.controllers;

import com.entrenaapp.entrenaapp_api.application.dto.EntrenamientoRequest;
import com.entrenaapp.entrenaapp_api.application.dto.EntrenamientoResponse;
import com.entrenaapp.entrenaapp_api.application.services.EntrenamientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entrenamientos")
@RequiredArgsConstructor
public class EntrenamientoController {

    private final EntrenamientoService entrenamientoService;

    @GetMapping
    public ResponseEntity<List<EntrenamientoResponse>> obtenerTodos() {
        return ResponseEntity.ok(entrenamientoService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<EntrenamientoResponse> crear(@Valid @RequestBody EntrenamientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entrenamientoService.crear(request));
    }
}
