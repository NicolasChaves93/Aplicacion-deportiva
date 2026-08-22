package com.entrenaapp.mobile.ui.entrenamiento;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.repository.EntrenamientoRepository;
import com.google.android.material.button.MaterialButton;

public class EntrenamientosFragment extends Fragment {

    private RecyclerView recyclerEntrenamientos;
    private TextView tvVacio;

    private EntrenamientoAdapter adapter;
    private EntrenamientoRepository entrenamientoRepository;
    private ActivityResultLauncher<Intent> formLauncher;

    public EntrenamientosFragment() {
        super(R.layout.fragment_entrenamientos);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> cargarEntrenamientos());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entrenamientoRepository = new EntrenamientoRepository(requireContext());
        adapter = new EntrenamientoAdapter(this::onEntrenamientoClick);

        recyclerEntrenamientos = view.findViewById(R.id.recyclerEntrenamientos);
        tvVacio = view.findViewById(R.id.tvVacio);
        MaterialButton btnAgregar = view.findViewById(R.id.btnAgregar);

        recyclerEntrenamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerEntrenamientos.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> formLauncher.launch(
                new Intent(requireContext(), EntrenamientoFormActivity.class)));

        cargarEntrenamientos();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarEntrenamientos();
    }

    private void cargarEntrenamientos() {
        var entrenamientos = entrenamientoRepository.obtenerTodos();
        adapter.actualizarDatos(entrenamientos);
        tvVacio.setVisibility(entrenamientos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onEntrenamientoClick(Entrenamiento entrenamiento) {
        formLauncher.launch(EntrenamientoDetailActivity.crearIntent(requireContext(), entrenamiento.getId()));
    }
}
