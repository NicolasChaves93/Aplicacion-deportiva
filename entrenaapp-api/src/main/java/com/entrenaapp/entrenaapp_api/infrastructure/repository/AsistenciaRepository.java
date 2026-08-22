package com.entrenaapp.entrenaapp_api.infrastructure.repository;

import com.entrenaapp.entrenaapp_api.domain.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, String> {
    List<Asistencia> findByEntrenamientoId(String entrenamientoId);
    List<Asistencia> findByDeportistaId(String deportistaId);
    long countByAsistioTrue();
}