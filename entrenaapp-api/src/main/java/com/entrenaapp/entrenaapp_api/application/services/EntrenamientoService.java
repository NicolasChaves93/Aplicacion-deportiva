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

    @Transactional
    public EntrenamientoResponse crear(EntrenamientoRequest request) {
        Entrenamiento entrenamiento = Entrenamiento.builder()
                .fecha(request.fecha())
                .tipo(request.tipo())
                .duracionMin(request.duracionMin())
                .intensidad(request.intensidad())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .observacionAudioPath(request.observacionAudioPath())
                .build();

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
