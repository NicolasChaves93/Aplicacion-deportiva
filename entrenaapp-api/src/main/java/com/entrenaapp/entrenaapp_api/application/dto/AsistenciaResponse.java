package com.entrenaapp.entrenaapp_api.application.dto;

public record AsistenciaResponse(
        String id,
        String entrenamientoId,
        String deportistaId,
        String deportistaNombre,
        Boolean asistio,
        String observacion
) {}
