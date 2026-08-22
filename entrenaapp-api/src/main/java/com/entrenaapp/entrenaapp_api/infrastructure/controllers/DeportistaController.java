package com.entrenaapp.entrenaapp_api.infrastructure.controllers;

import com.entrenaapp.entrenaapp_api.application.dto.DeportistaRequest;
import com.entrenaapp.entrenaapp_api.application.dto.DeportistaResponse;
import com.entrenaapp.entrenaapp_api.application.services.DeportistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deportistas")
@RequiredArgsConstructor
public class DeportistaController {

    private final DeportistaService deportistaService;

    @GetMapping
    public ResponseEntity<List<DeportistaResponse>> obtenerTodos() {
        return ResponseEntity.ok(deportistaService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeportistaResponse> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(deportistaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<DeportistaResponse> crear(@Valid @RequestBody DeportistaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deportistaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeportistaResponse> actualizar(@PathVariable String id,
                                                           @Valid @RequestBody DeportistaRequest request) {
        return ResponseEntity.ok(deportistaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable String id) {
        deportistaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
