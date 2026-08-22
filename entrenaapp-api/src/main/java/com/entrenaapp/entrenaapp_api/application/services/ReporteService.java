package com.entrenaapp.entrenaapp_api.application.services;

import com.entrenaapp.entrenaapp_api.application.dto.CargaDiaResponse;
import com.entrenaapp.entrenaapp_api.application.dto.DashboardStatsResponse;
import com.entrenaapp.entrenaapp_api.application.dto.EntrenamientoResponse;
import com.entrenaapp.entrenaapp_api.domain.model.Entrenamiento;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.AsistenciaRepository;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.DeportistaRepository;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.EntrenamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Agregaciones simples sobre Deportista/Entrenamiento/Asistencia. No agrega
// tablas nuevas: todo se calcula al vuelo sobre lo que ya existe.
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final DeportistaRepository deportistaRepository;
    private final EntrenamientoRepository entrenamientoRepository;
    private final AsistenciaRepository asistenciaRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse obtenerStats() {
        long totalDeportistas = deportistaRepository.countByActivoTrue();
        long totalEntrenamientos = entrenamientoRepository.count();

        long totalAsistencias = asistenciaRepository.count();
        long presentes = asistenciaRepository.countByAsistioTrue();
        double asistenciaPromedio = totalAsistencias == 0 ? 0.0 : (presentes * 100.0) / totalAsistencias;

        Entrenamiento proximo = entrenamientoRepository
                .findFirstByFechaGreaterThanEqualOrderByFechaAsc(LocalDate.now())
                .orElse(null);

        return new DashboardStatsResponse(
                totalDeportistas,
                totalEntrenamientos,
                Math.round(asistenciaPromedio * 10) / 10.0,
                proximo != null ? mapToEntrenamientoResponse(proximo) : null
        );
    }

    @Transactional(readOnly = true)
    public List<CargaDiaResponse> obtenerCargaSemanal() {
        LocalDate hoy = LocalDate.now();
        LocalDate haceSeisDias = hoy.minusDays(6);

        List<Entrenamiento> entrenamientos = entrenamientoRepository.findByFechaBetween(haceSeisDias, hoy);
        Map<LocalDate, Long> conteoPorDia = entrenamientos.stream()
                .collect(Collectors.groupingBy(Entrenamiento::getFecha, Collectors.counting()));

        List<CargaDiaResponse> resultado = new ArrayList<>();
        for (LocalDate fecha = haceSeisDias; !fecha.isAfter(hoy); fecha = fecha.plusDays(1)) {
            resultado.add(new CargaDiaResponse(fecha, conteoPorDia.getOrDefault(fecha, 0L)));
        }
        return resultado;
    }

    private EntrenamientoResponse mapToEntrenamientoResponse(Entrenamiento e) {
        return new EntrenamientoResponse(
                e.getId(), e.getFecha(), e.getTipo(), e.getDuracionMin(),
                e.getIntensidad(), e.getLatitud(), e.getLongitud(), e.getObservacionAudioPath());
    }
}
