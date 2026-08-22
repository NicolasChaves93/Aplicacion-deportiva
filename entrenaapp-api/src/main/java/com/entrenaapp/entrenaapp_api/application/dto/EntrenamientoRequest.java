package com.entrenaapp.entrenaapp_api.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EntrenamientoRequest(
        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
        LocalDate fecha,

        @NotBlank(message = "El tipo es obligatorio")
        String tipo,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 1, message = "La duración debe ser mayor a 0 minutos")
        Integer duracionMin,

        @NotBlank(message = "La intensidad es obligatoria")
        String intensidad,

        Double latitud,
        Double longitud,
        String observacionAudioPath
) {}