package com.entrenaapp.entrenaapp_api.application.services;

import com.entrenaapp.entrenaapp_api.application.dto.AsistenciaRequest;
import com.entrenaapp.entrenaapp_api.application.dto.AsistenciaResponse;
import com.entrenaapp.entrenaapp_api.domain.model.Asistencia;
import com.entrenaapp.entrenaapp_api.domain.model.Deportista;
import com.entrenaapp.entrenaapp_api.domain.model.Entrenamiento;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.AsistenciaRepository;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.DeportistaRepository;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.EntrenamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final EntrenamientoRepository entrenamientoRepository;
    private final DeportistaRepository deportistaRepository;

    // Upsert: si request.id() ya existe (ej. el entrenador cambio la marca de
    // un deportista y esa asistencia se vuelve a sincronizar), se actualiza
    // esa fila en vez de crear una nueva.
    @Transactional
    public AsistenciaResponse registrarAsistencia(AsistenciaRequest request) {
        Entrenamiento entrenamiento = entrenamientoRepository.findById(request.entrenamientoId())
                .orElseThrow(() -> new RuntimeException("Entrenamiento no encontrado"));

        Deportista deportista = deportistaRepository.findById(request.deportistaId())
                .orElseThrow(() -> new RuntimeException("Deportista no encontrado"));

        Asistencia asistencia = request.id() != null
                ? asistenciaRepository.findById(request.id()).orElse(null)
                : null;

        if (asistencia == null) {
            asistencia = Asistencia.builder().id(request.id()).build();
        }

        asistencia.setEntrenamiento(entrenamiento);
        asistencia.setDeportista(deportista);
        asistencia.setAsistio(request.asistio());
        asistencia.setObservacion(request.observacion());

        return mapToResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> obtenerPorEntrenamiento(String entrenamientoId) {
        return asistenciaRepository.findByEntrenamientoId(entrenamientoId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AsistenciaResponse mapToResponse(Asistencia a) {
        return new AsistenciaResponse(
                a.getId(),
                a.getEntrenamiento().getId(),
                a.getDeportista().getId(),
                a.getDeportista().getNombre(),
                a.getAsistio(),
                a.getObservacion()
        );
    }
}
