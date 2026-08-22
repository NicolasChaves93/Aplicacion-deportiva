package com.entrenaapp.entrenaapp_api.application.dto;

import java.time.LocalDate;

public record CargaDiaResponse(
        LocalDate fecha,
        long cantidadEntrenamientos
) {}
