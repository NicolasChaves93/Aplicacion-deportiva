package com.entrenaapp.entrenaapp_api.infrastructure.controllers;

import com.entrenaapp.entrenaapp_api.application.dto.AuthResponse;
import com.entrenaapp.entrenaapp_api.application.dto.LoginRequest;
import com.entrenaapp.entrenaapp_api.application.dto.RegistroRequest;
import com.entrenaapp.entrenaapp_api.application.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
