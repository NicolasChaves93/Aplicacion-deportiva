package com.entrenaapp.entrenaapp_api.application.dto;

public record DashboardStatsResponse(
        long totalDeportistas,
        long totalEntrenamientos,
        double asistenciaPromedioPorcentaje,
        EntrenamientoResponse proximoEntrenamiento
) {}
