package com.entrenaapp.entrenaapp_api.infrastructure.repository;

import com.entrenaapp.entrenaapp_api.domain.model.Deportista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeportistaRepository extends JpaRepository<Deportista, String> {
    List<Deportista> findByActivoTrue();
    Optional<Deportista> findByDocumento(String documento);
    long countByActivoTrue();
}
