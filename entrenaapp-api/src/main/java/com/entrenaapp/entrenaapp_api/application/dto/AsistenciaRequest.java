package com.entrenaapp.entrenaapp_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AsistenciaRequest(
        // Opcional: ver DeportistaRequest.id.
        String id,

        @NotBlank(message = "El id de entrenamiento es obligatorio")
        String entrenamientoId,

        @NotBlank(message = "El id de deportista es obligatorio")
        String deportistaId,

        @NotNull(message = "El estado de asistencia es obligatorio")
        Boolean asistio,

        String observacion
) {}
