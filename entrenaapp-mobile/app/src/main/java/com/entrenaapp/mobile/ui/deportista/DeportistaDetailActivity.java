package com.entrenaapp.mobile.ui.deportista;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.data.local.repository.DeportistaRepository;
import com.entrenaapp.mobile.util.ImageUtils;

public class DeportistaDetailActivity extends AppCompatActivity {

    private static final String EXTRA_DEPORTISTA_ID = "deportista_id";

    public static Intent crearIntent(Context context, String deportistaId) {
        Intent intent = new Intent(context, DeportistaDetailActivity.class);
        intent.putExtra(EXTRA_DEPORTISTA_ID, deportistaId);
        return intent;
    }

    private DeportistaRepository deportistaRepository;
    private String deportistaId;
    private ActivityResultLauncher<Intent> editarLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deportista_detail);

        deportistaRepository = new DeportistaRepository(this);
        editarLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> cargarYMostrar());

        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());
        findViewById(R.id.btnEditar).setOnClickListener(v -> editarLauncher.launch(
                DeportistaFormActivity.crearIntentEditar(this, deportistaId)));

        deportistaId = getIntent().getStringExtra(EXTRA_DEPORTISTA_ID);
        if (deportistaId == null) {
            finish();
            return;
        }

        cargarYMostrar();
    }

    private void cargarYMostrar() {
        Deportista deportista = deportistaRepository.obtenerPorId(deportistaId);
        if (deportista == null || !deportista.isActivo()) {
            // No existe o se desactivo (soft-delete) desde la pantalla de edicion
            finish();
            return;
        }
        mostrarDeportista(deportista);
    }

    private void mostrarDeportista(Deportista deportista) {
        ((TextView) findViewById(R.id.tvNombre)).setText(deportista.getNombre());
        ((TextView) findViewById(R.id.tvDisciplina)).setText(deportista.getDisciplina());
        ((TextView) findViewById(R.id.tvDocumento)).setText(deportista.getDocumento());
        ((TextView) findViewById(R.id.tvEdad)).setText(
                getString(R.string.deportista_detalle_anios, deportista.getEdad()));

        String fotoPath = deportista.getFotoPath();
        Bitmap bitmap = fotoPath != null
                ? ImageUtils.cargarBitmapRotado(fotoPath, 400, 400)
                : null;

        ImageView ivFoto = findViewById(R.id.ivFoto);
        if (bitmap != null) {
            ivFoto.setPadding(0, 0, 0, 0);
            ivFoto.setImageBitmap(bitmap);
        } else {
            ivFoto.setPadding(36, 36, 36, 36);
            ivFoto.setImageResource(R.drawable.ic_person_placeholder);
        }
    }
}
