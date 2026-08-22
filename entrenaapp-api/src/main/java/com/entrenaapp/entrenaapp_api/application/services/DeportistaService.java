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
        return deportistaRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeportistaResponse obtenerPorId(String id) {
        return mapToResponse(buscarPorId(id));
    }

    // Si el documento pertenece a un deportista desactivado (soft-delete), se
    // reactiva esa misma fila con los datos nuevos en vez de crear una fila
    // nueva; el UNIQUE de documento no lo permitiria de todas formas.
    @Transactional
    public DeportistaResponse crear(DeportistaRequest request) {
        Deportista existente = deportistaRepository.findByDocumento(request.documento()).orElse(null);

        if (existente != null && Boolean.TRUE.equals(existente.getActivo())) {
            throw new RuntimeException("Ya existe un deportista con ese documento");
        }

        if (existente != null) {
            existente.setNombre(request.nombre());
            existente.setEdad(request.edad());
            existente.setDisciplina(request.disciplina());
            existente.setFotoPath(request.fotoPath());
            existente.setActivo(true);
            return mapToResponse(deportistaRepository.save(existente));
        }

        Deportista deportista = Deportista.builder()
                .nombre(request.nombre())
                .documento(request.documento())
                .edad(request.edad())
                .disciplina(request.disciplina())
                .fotoPath(request.fotoPath())
                .build();

        return mapToResponse(deportistaRepository.save(deportista));
    }

    @Transactional
    public DeportistaResponse actualizar(String id, DeportistaRequest request) {
        Deportista deportista = buscarPorId(id);

        deportistaRepository.findByDocumento(request.documento())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new RuntimeException("Ya existe un deportista con ese documento");
                });

        deportista.setNombre(request.nombre());
        deportista.setDocumento(request.documento());
        deportista.setEdad(request.edad());
        deportista.setDisciplina(request.disciplina());
        deportista.setFotoPath(request.fotoPath());

        return mapToResponse(deportistaRepository.save(deportista));
    }

    // Soft-delete: desactiva el deportista para conservarlo con fines de
    // auditoria en lugar de borrar la fila.
    @Transactional
    public void desactivar(String id) {
        Deportista deportista = buscarPorId(id);
        deportista.setActivo(false);
        deportistaRepository.save(deportista);
    }

    private Deportista buscarPorId(String id) {
        return deportistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deportista no encontrado"));
    }

    private DeportistaResponse mapToResponse(Deportista d) {
        return new DeportistaResponse(
                d.getId(),
                d.getNombre(),
                d.getDocumento(),
                d.getEdad(),
                d.getDisciplina(),
                d.getFotoPath(),
                d.getActivo()
        );
    }
}
