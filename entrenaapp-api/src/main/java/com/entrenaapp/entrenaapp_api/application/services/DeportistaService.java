package com.entrenaapp.entrenaapp_api.application.services;

import com.entrenaapp.entrenaapp_api.application.dto.DeportistaRequest;
import com.entrenaapp.entrenaapp_api.application.dto.DeportistaResponse;
import com.entrenaapp.entrenaapp_api.domain.model.Deportista;
import com.entrenaapp.entrenaapp_api.infrastructure.exception.RecursoNoEncontradoException;
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

    // Upsert: si request.id() ya existe, es una sincronizacion desde el
    // celular de un registro que ya se habia subido antes -> se actualiza esa
    // misma fila. Si no, y el documento pertenece a un deportista desactivado
    // (soft-delete), se reactiva esa fila con los datos nuevos (el UNIQUE de
    // documento no permitiria insertar una fila nueva de todas formas). Si
    // ninguno de los dos aplica, se crea un deportista nuevo.
    @Transactional
    public DeportistaResponse crear(DeportistaRequest request) {
        Deportista objetivo = request.id() != null
                ? deportistaRepository.findById(request.id()).orElse(null)
                : null;

        if (objetivo == null) {
            Deportista porDocumento = deportistaRepository.findByDocumento(request.documento()).orElse(null);
            if (porDocumento != null && Boolean.TRUE.equals(porDocumento.getActivo())) {
                throw new RuntimeException("Ya existe un deportista con ese documento");
            }
            objetivo = porDocumento != null ? porDocumento : Deportista.builder().id(request.id()).build();
        }

        objetivo.setNombre(request.nombre());
        objetivo.setDocumento(request.documento());
        objetivo.setEdad(request.edad());
        objetivo.setDisciplina(request.disciplina());
        objetivo.setFotoPath(request.fotoPath());
        objetivo.setActivo(true);

        return mapToResponse(deportistaRepository.save(objetivo));
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Deportista no encontrado"));
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
