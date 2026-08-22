package com.entrenaapp.entrenaapp_api.application.services;

import com.entrenaapp.entrenaapp_api.application.dto.EntrenamientoRequest;
import com.entrenaapp.entrenaapp_api.application.dto.EntrenamientoResponse;
import com.entrenaapp.entrenaapp_api.domain.model.Entrenamiento;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.EntrenamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntrenamientoService {

    private final EntrenamientoRepository entrenamientoRepository;

    @Transactional(readOnly = true)
    public List<EntrenamientoResponse> obtenerTodos() {
        return entrenamientoRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Upsert: si request.id() ya existe (sincronizacion de un entrenamiento
    // ya subido antes, ej. tras editarlo localmente), se actualiza esa fila
    // en vez de crear una nueva.
    @Transactional
    public EntrenamientoResponse crear(EntrenamientoRequest request) {
        Entrenamiento entrenamiento = request.id() != null
                ? entrenamientoRepository.findById(request.id()).orElse(null)
                : null;

        if (entrenamiento == null) {
            entrenamiento = Entrenamiento.builder().id(request.id()).build();
        }

        entrenamiento.setFecha(request.fecha());
        entrenamiento.setTipo(request.tipo());
        entrenamiento.setDuracionMin(request.duracionMin());
        entrenamiento.setIntensidad(request.intensidad());
        entrenamiento.setLatitud(request.latitud());
        entrenamiento.setLongitud(request.longitud());
        entrenamiento.setObservacionAudioPath(request.observacionAudioPath());

        return mapToResponse(entrenamientoRepository.save(entrenamiento));
    }

    private EntrenamientoResponse mapToResponse(Entrenamiento e) {
        return new EntrenamientoResponse(
                e.getId(),
                e.getFecha(),
                e.getTipo(),
                e.getDuracionMin(),
                e.getIntensidad(),
                e.getLatitud(),
                e.getLongitud(),
                e.getObservacionAudioPath()
        );
    }
}
