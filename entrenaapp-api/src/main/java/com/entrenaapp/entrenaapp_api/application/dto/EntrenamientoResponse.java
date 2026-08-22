package com.entrenaapp.entrenaapp_api.application.dto;

import java.time.LocalDate;

public record EntrenamientoResponse(
        String id,
        LocalDate fecha,
        String tipo,
        Integer duracionMin,
        String intensidad,
        Double latitud,
        Double longitud,
        String observacionAudioPath
) {}
