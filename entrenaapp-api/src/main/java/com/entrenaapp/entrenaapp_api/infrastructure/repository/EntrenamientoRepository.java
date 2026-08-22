package com.entrenaapp.entrenaapp_api.infrastructure.repository;

import com.entrenaapp.entrenaapp_api.domain.model.Entrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, String> {
    Optional<Entrenamiento> findFirstByFechaGreaterThanEqualOrderByFechaAsc(LocalDate fecha);
    List<Entrenamiento> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
