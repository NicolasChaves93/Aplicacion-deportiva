package com.entrenaapp.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Asistencia;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.repository.AsistenciaRepository;
import com.entrenaapp.mobile.data.local.repository.DeportistaRepository;
import com.entrenaapp.mobile.data.local.repository.EntrenamientoRepository;
import com.entrenaapp.mobile.data.session.SessionManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard de inicio. Todas las estadisticas se calculan de forma local
 * sobre SQLite (no se consulta la API) para mantener la app coherente con
 * el resto de pantallas, que funcionan sin conexion.
 */
public class HomeFragment extends Fragment {

    private SessionManager sessionManager;
    private DeportistaRepository deportistaRepository;
    private EntrenamientoRepository entrenamientoRepository;
    private AsistenciaRepository asistenciaRepository;

    private TextView tvBienvenida;
    private TextView tvStatDeportistas;
    private TextView tvStatEntrenamientos;
    private TextView tvStatAsistencia;
    private TextView tvProximoTipo;
    private TextView tvProximoFecha;
    private WeeklyBarChartView chartCargaSemanal;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        deportistaRepository = new DeportistaRepository(requireContext());
        entrenamientoRepository = new EntrenamientoRepository(requireContext());
        asistenciaRepository = new AsistenciaRepository(requireContext());

        tvBienvenida = view.findViewById(R.id.tvBienvenida);
        tvStatDeportistas = view.findViewById(R.id.tvStatDeportistas);
        tvStatEntrenamientos = view.findViewById(R.id.tvStatEntrenamientos);
        tvStatAsistencia = view.findViewById(R.id.tvStatAsistencia);
        tvProximoTipo = view.findViewById(R.id.tvProximoTipo);
        tvProximoFecha = view.findViewById(R.id.tvProximoFecha);
        chartCargaSemanal = view.findViewById(R.id.chartCargaSemanal);

        String nombre = sessionManager.getNombre();
        tvBienvenida.setText(getString(R.string.home_bienvenida, nombre != null ? nombre : ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDashboard();
    }

    private void cargarDashboard() {
        List<Entrenamiento> entrenamientos = entrenamientoRepository.obtenerTodos();

        tvStatDeportistas.setText(String.valueOf(deportistaRepository.obtenerTodos().size()));
        tvStatEntrenamientos.setText(String.valueOf(entrenamientos.size()));
        tvStatAsistencia.setText(getString(R.string.home_stat_asistencia_valor, calcularAsistenciaPromedio(entrenamientos)));

        mostrarProximoEntrenamiento(entrenamientos);
        mostrarCargaSemanal(entrenamientos);
    }

    private double calcularAsistenciaPromedio(List<Entrenamiento> entrenamientos) {
        int total = 0;
        int presentes = 0;
        for (Entrenamiento entrenamiento : entrenamientos) {
            for (Asistencia asistencia : asistenciaRepository.obtenerPorEntrenamiento(entrenamiento.getId())) {
                total++;
                if (asistencia.isAsistio()) {
                    presentes++;
                }
            }
        }
        return total == 0 ? 0.0 : (presentes * 100.0) / total;
    }

    private void mostrarProximoEntrenamiento(List<Entrenamiento> entrenamientos) {
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        Entrenamiento proximo = null;
        for (Entrenamiento entrenamiento : entrenamientos) {
            LocalDate fecha = parsearFecha(entrenamiento.getFecha());
            if (fecha == null || fecha.isBefore(hoy)) {
                continue;
            }
            if (proximo == null || fecha.isBefore(parsearFecha(proximo.getFecha()))) {
                proximo = entrenamiento;
            }
        }

        if (proximo == null) {
            tvProximoTipo.setText(R.string.home_stat_proximo_vacio);
            tvProximoFecha.setText("");
        } else {
            tvProximoTipo.setText(proximo.getTipo());
            tvProximoFecha.setText(proximo.getFecha());
        }
    }

    private void mostrarCargaSemanal(List<Entrenamiento> entrenamientos) {
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        LocalDate haceSeisDias = hoy.minusDays(6);

        List<Integer> valores = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        for (LocalDate fecha = haceSeisDias; !fecha.isAfter(hoy); fecha = fecha.plusDays(1)) {
            int cantidad = 0;
            for (Entrenamiento entrenamiento : entrenamientos) {
                if (fecha.equals(parsearFecha(entrenamiento.getFecha()))) {
                    cantidad++;
                }
            }
            valores.add(cantidad);
            String etiqueta = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            etiquetas.add(capitalizar(etiqueta));
        }

        chartCargaSemanal.setDatos(valores, etiquetas);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(Locale.getDefault()) + texto.substring(1);
    }

    @Nullable
    private LocalDate parsearFecha(String fecha) {
        try {
            return fecha != null ? LocalDate.parse(fecha) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
