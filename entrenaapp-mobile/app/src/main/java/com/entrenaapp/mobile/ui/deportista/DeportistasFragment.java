package com.entrenaapp.mobile.ui.deportista;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.data.local.repository.DeportistaRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeportistasFragment extends Fragment {

    private RecyclerView recyclerDeportistas;
    private TextView tvVacio;
    private EditText etBuscar;

    private DeportistaAdapter adapter;
    private DeportistaRepository deportistaRepository;
    private List<Deportista> todosLosDeportistas = new ArrayList<>();

    private ActivityResultLauncher<Intent> formLauncher;

    public DeportistasFragment() {
        super(R.layout.fragment_deportistas);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> cargarDeportistas());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deportistaRepository = new DeportistaRepository(requireContext());
        adapter = new DeportistaAdapter(this::onDeportistaClick);

        recyclerDeportistas = view.findViewById(R.id.recyclerDeportistas);
        tvVacio = view.findViewById(R.id.tvVacio);
        etBuscar = view.findViewById(R.id.etBuscar);
        MaterialButton btnAgregar = view.findViewById(R.id.btnAgregar);

        recyclerDeportistas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDeportistas.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> formLauncher.launch(
                new Intent(requireContext(), DeportistaFormActivity.class)));

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        cargarDeportistas();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDeportistas();
    }

    private void cargarDeportistas() {
        todosLosDeportistas = deportistaRepository.obtenerTodos();
        filtrar(etBuscar.getText() != null ? etBuscar.getText().toString() : "");
    }

    private void filtrar(String consulta) {
        String consultaNormalizada = consulta.trim().toLowerCase(Locale.ROOT);
        List<Deportista> filtrados = new ArrayList<>();
        for (Deportista deportista : todosLosDeportistas) {
            boolean coincideNombre = deportista.getNombre().toLowerCase(Locale.ROOT).contains(consultaNormalizada);
            boolean coincideDocumento = deportista.getDocumento().toLowerCase(Locale.ROOT).contains(consultaNormalizada);
            if (coincideNombre || coincideDocumento) {
                filtrados.add(deportista);
            }
        }

        adapter.actualizarDatos(filtrados);

        boolean vacio = filtrados.isEmpty();
        tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        tvVacio.setText(todosLosDeportistas.isEmpty()
                ? R.string.deportistas_vacio
                : R.string.deportistas_sin_resultados);
    }

    private void onDeportistaClick(Deportista deportista) {
        formLauncher.launch(DeportistaDetailActivity.crearIntent(requireContext(), deportista.getId()));
    }
}
