package com.entrenaapp.mobile.ui.asistencia;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Asistencia;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.repository.AsistenciaRepository;
import com.entrenaapp.mobile.data.local.repository.DeportistaRepository;
import com.entrenaapp.mobile.data.local.repository.EntrenamientoRepository;
import com.entrenaapp.mobile.data.sync.SyncScheduler;
import com.entrenaapp.mobile.util.ConnectivityObserver;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaFragment extends Fragment {

    private TextView tvSesionSeleccionada;
    private TextView tvStatTotal;
    private TextView tvStatAsistieron;
    private TextView tvStatNoAsistieron;
    private TextView tvStatPendientes;
    private TextView tvVacio;
    private View rowSesion;
    private View rowStats;
    private RecyclerView recyclerAsistencia;
    private MaterialButton btnRegistrar;

    private DeportistaRepository deportistaRepository;
    private EntrenamientoRepository entrenamientoRepository;
    private AsistenciaRepository asistenciaRepository;
    private AsistenciaAdapter adapter;

    private List<Entrenamiento> entrenamientos;
    private Entrenamiento entrenamientoSeleccionado;

    public AsistenciaFragment() {
        super(R.layout.fragment_asistencia);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deportistaRepository = new DeportistaRepository(requireContext());
        entrenamientoRepository = new EntrenamientoRepository(requireContext());
        asistenciaRepository = new AsistenciaRepository(requireContext());
        adapter = new AsistenciaAdapter(this::actualizarStats);

        initObjects(view);
        recyclerAsistencia.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAsistencia.setAdapter(adapter);

        rowSesion.setOnClickListener(v -> mostrarSelectorSesion());
        btnRegistrar.setOnClickListener(v -> onRegistrarClick());

        cargarDatos();
    }

    @Override
    public void onResume() {
        super.onResume();
        SyncScheduler.solicitarSincronizacion(requireContext());
        cargarDatos();
    }

    private void cargarDatos() {
        entrenamientos = entrenamientoRepository.obtenerTodos();

        if (entrenamientos.isEmpty()) {
            mostrarEstadoVacio(getString(R.string.asistencia_sin_entrenamientos));
            return;
        }

        String idPrevio = entrenamientoSeleccionado != null ? entrenamientoSeleccionado.getId() : null;
        entrenamientoSeleccionado = buscarPorId(idPrevio);
        if (entrenamientoSeleccionado == null) {
            entrenamientoSeleccionado = entrenamientos.get(0);
        }

        List<Deportista> deportistas = deportistaRepository.obtenerTodos();
        if (deportistas.isEmpty()) {
            mostrarEstadoVacio(getString(R.string.asistencia_sin_deportistas));
            return;
        }

        mostrarContenido();
        cargarSesion(entrenamientoSeleccionado, deportistas);
    }

    private Entrenamiento buscarPorId(String id) {
        if (id == null) {
            return null;
        }
        for (Entrenamiento entrenamiento : entrenamientos) {
            if (entrenamiento.getId().equals(id)) {
                return entrenamiento;
            }
        }
        return null;
    }

    private void cargarSesion(Entrenamiento entrenamiento, List<Deportista> deportistas) {
        tvSesionSeleccionada.setText(entrenamiento.getTipo() + " · " + entrenamiento.getFecha());

        Map<String, Boolean> estados = new HashMap<>();
        for (Asistencia asistencia : asistenciaRepository.obtenerPorEntrenamiento(entrenamiento.getId())) {
            estados.put(asistencia.getDeportistaId(), asistencia.isAsistio());
        }

        adapter.actualizarDatos(deportistas, estados);
        actualizarStats();
    }

    private void mostrarSelectorSesion() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), rowSesion);
        for (int i = 0; i < entrenamientos.size(); i++) {
            Entrenamiento entrenamiento = entrenamientos.get(i);
            popupMenu.getMenu().add(0, i, i, entrenamiento.getTipo() + " · " + entrenamiento.getFecha());
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            entrenamientoSeleccionado = entrenamientos.get(item.getItemId());
            cargarSesion(entrenamientoSeleccionado, deportistaRepository.obtenerTodos());
            return true;
        });
        popupMenu.show();
    }

    private void actualizarStats() {
        int total = adapter.contarTotal();
        int confirmados = adapter.contarConfirmados();
        int presentes = adapter.contarPresentes();
        int ausentes = adapter.contarAusentes();
        int pendientes = total - confirmados;

        tvStatTotal.setText(String.format("%02d", total));
        tvStatAsistieron.setText(String.format("%02d", presentes));
        tvStatNoAsistieron.setText(String.format("%02d", ausentes));
        tvStatPendientes.setText(String.format("%02d", pendientes));
    }

    private void onRegistrarClick() {
        Map<String, Boolean> estados = adapter.getEstados();
        if (estados.isEmpty()) {
            Toast.makeText(requireContext(), R.string.asistencia_sin_marcar, Toast.LENGTH_SHORT).show();
            return;
        }

        for (Map.Entry<String, Boolean> entry : estados.entrySet()) {
            Asistencia asistencia = new Asistencia(
                    null, entrenamientoSeleccionado.getId(), entry.getKey(), entry.getValue(), null);
            asistenciaRepository.guardarAsistencia(asistencia);
        }

        int mensaje = ConnectivityObserver.hayConexion(requireContext())
                ? R.string.asistencia_guardada
                : R.string.sync_guardado_offline;
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    private void mostrarEstadoVacio(String mensaje) {
        tvVacio.setText(mensaje);
        tvVacio.setVisibility(View.VISIBLE);
        rowSesion.setVisibility(View.GONE);
        rowStats.setVisibility(View.GONE);
        recyclerAsistencia.setVisibility(View.GONE);
        btnRegistrar.setVisibility(View.GONE);
    }

    private void mostrarContenido() {
        tvVacio.setVisibility(View.GONE);
        rowSesion.setVisibility(View.VISIBLE);
        rowStats.setVisibility(View.VISIBLE);
        recyclerAsistencia.setVisibility(View.VISIBLE);
        btnRegistrar.setVisibility(View.VISIBLE);
    }

    private void initObjects(View view) {
        tvSesionSeleccionada = view.findViewById(R.id.tvSesionSeleccionada);
        tvStatTotal = view.findViewById(R.id.tvStatTotal);
        tvStatAsistieron = view.findViewById(R.id.tvStatAsistieron);
        tvStatNoAsistieron = view.findViewById(R.id.tvStatNoAsistieron);
        tvStatPendientes = view.findViewById(R.id.tvStatPendientes);
        tvVacio = view.findViewById(R.id.tvVacio);
        rowSesion = view.findViewById(R.id.rowSesion);
        rowStats = view.findViewById(R.id.rowStats);
        recyclerAsistencia = view.findViewById(R.id.recyclerAsistencia);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);
    }
}
