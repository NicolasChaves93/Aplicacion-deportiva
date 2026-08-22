package com.entrenaapp.entrenaapp_api.application.services;

import com.entrenaapp.entrenaapp_api.application.dto.DeportistaRequest;
import com.entrenaapp.entrenaapp_api.application.dto.DeportistaResponse;
import com.entrenaapp.entrenaapp_api.domain.model.Deportista;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.DeportistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeportistaService {

    private final DeportistaRepository deportistaRepository;

    @Transactional(readOnly = true)
    public List<DeportistaResponse> obtenerTodos() {
        return deportistaRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeportistaResponse obtenerPorId(String id) {
        Deportista deportista = deportistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deportista no encontrado"));
        return mapToResponse(deportista);
    }

    @Transactional
    public DeportistaResponse crear(DeportistaRequest request) {
        if (deportistaRepository.existsByDocumento(request.documento())) {
            throw new RuntimeException("Ya existe un deportista con ese documento");
        }

        Deportista deportista = Deportista.builder()
                .nombre(request.nombre())
                .documento(request.documento())
                .edad(request.edad())
                .disciplina(request.disciplina())
                .fotoPath(request.fotoPath())
                .build();

        Deportista guardado = deportistaRepository.save(deportista);
        return mapToResponse(guardado);
    }

    private DeportistaResponse mapToResponse(Deportista d) {
        return new DeportistaResponse(
                d.getId(),
                d.getNombre(),
                d.getDocumento(),
                d.getEdad(),
                d.getDisciplina(),
                d.getFotoPath()
        );
    }
}