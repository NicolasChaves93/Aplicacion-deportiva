package com.entrenaapp.entrenaapp_api.infrastructure.controllers;

import com.entrenaapp.entrenaapp_api.application.dto.CargaDiaResponse;
import com.entrenaapp.entrenaapp_api.application.dto.DashboardStatsResponse;
import com.entrenaapp.entrenaapp_api.application.services.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/api/v1/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> obtenerStats() {
        return ResponseEntity.ok(reporteService.obtenerStats());
    }

    @GetMapping("/api/v1/reportes/carga")
    public ResponseEntity<List<CargaDiaResponse>> obtenerCargaSemanal() {
        return ResponseEntity.ok(reporteService.obtenerCargaSemanal());
    }
}
