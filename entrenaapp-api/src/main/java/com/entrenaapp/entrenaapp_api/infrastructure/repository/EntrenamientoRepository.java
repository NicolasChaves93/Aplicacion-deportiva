package com.entrenaapp.entrenaapp_api.infrastructure.repository;

import com.entrenaapp.entrenaapp_api.domain.model.Entrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, String> {
}
