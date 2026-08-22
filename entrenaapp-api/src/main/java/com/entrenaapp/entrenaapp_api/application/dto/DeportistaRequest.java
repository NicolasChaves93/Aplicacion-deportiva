package com.entrenaapp.entrenaapp_api.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeportistaRequest(
        // Opcional: si viene (sincronizacion desde el celular), se usa tal
        // cual como id de la fila en vez de generar uno nuevo.
        String id,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El documento es obligatorio")
        String documento,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 3, message = "La edad mínima es 3 años")
        Integer edad,

        @NotBlank(message = "La disciplina es obligatoria")
        String disciplina,

        String fotoPath
) {}
