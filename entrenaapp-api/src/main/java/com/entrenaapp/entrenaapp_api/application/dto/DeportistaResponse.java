package com.entrenaapp.entrenaapp_api.application.dto;

public record DeportistaResponse(
        String id,
        String nombre,
        String documento,
        Integer edad,
        String disciplina,
        String fotoPath
) {}
