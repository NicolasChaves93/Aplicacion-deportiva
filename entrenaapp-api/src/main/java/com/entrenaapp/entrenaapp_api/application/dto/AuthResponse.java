package com.entrenaapp.entrenaapp_api.application.dto;

public record AuthResponse(
        String token,
        String id,
        String nombre,
        String email,
        String rol
) {}
