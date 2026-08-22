package com.entrenaapp.mobile.ui.entrenamiento;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.repository.EntrenamientoRepository;
import com.entrenaapp.mobile.util.GeocodingUtils;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;

public class EntrenamientoDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ENTRENAMIENTO_ID = "entrenamiento_id";

    public static Intent crearIntent(Context context, String entrenamientoId) {
        Intent intent = new Intent(context, EntrenamientoDetailActivity.class);
        intent.putExtra(EXTRA_ENTRENAMIENTO_ID, entrenamientoId);
        return intent;
    }

    private EntrenamientoRepository entrenamientoRepository;
    private MaterialButton btnReproducir;
    private MediaPlayer mediaPlayer;
    private boolean reproduciendo;
    private String audioPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento_detail);

        entrenamientoRepository = new EntrenamientoRepository(this);
        btnReproducir = findViewById(R.id.btnReproducir);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());
        btnReproducir.setOnClickListener(v -> onReproducirClick());
        findViewById(R.id.btnEliminar).setOnClickListener(v -> onEliminarClick());

        String entrenamientoId = getIntent().getStringExtra(EXTRA_ENTRENAMIENTO_ID);
        Entrenamiento entrenamiento = entrenamientoId != null
                ? entrenamientoRepository.obtenerPorId(entrenamientoId)
                : null;

        if (entrenamiento == null) {
            finish();
            return;
        }

        mostrarEntrenamiento(entrenamiento);
    }

    private void mostrarEntrenamiento(Entrenamiento entrenamiento) {
        ((TextView) findViewById(R.id.tvTipo)).setText(entrenamiento.getTipo());
        ((TextView) findViewById(R.id.tvFecha)).setText(entrenamiento.getFecha());
        ((TextView) findViewById(R.id.tvDuracion)).setText(
                getString(R.string.entrenamiento_detalle_duracion, entrenamiento.getDuracionMin()));
        ((TextView) findViewById(R.id.tvIntensidad)).setText(
                getString(R.string.entrenamiento_detalle_intensidad, entrenamiento.getIntensidad()));

        TextView tvUbicacion = findViewById(R.id.tvUbicacion);
        Double latitud = entrenamiento.getLatitud();
        Double longitud = entrenamiento.getLongitud();
        if (latitud != null && longitud != null) {
            tvUbicacion.setText(getString(
                    R.string.entrenamiento_form_ubicacion_capturada, latitud, longitud));
            GeocodingUtils.obtenerDireccion(this, latitud, longitud, direccion -> {
                if (direccion != null) {
                    tvUbicacion.setText(direccion);
                }
            });
        } else {
            tvUbicacion.setText(R.string.entrenamiento_detalle_sin_ubicacion);
        }

        TextView tvAudioEstado = findViewById(R.id.tvAudioEstado);
        audioPath = entrenamiento.getAudioPath();
        boolean hayAudio = !TextUtils.isEmpty(audioPath) && new File(audioPath).exists();
        tvAudioEstado.setText(hayAudio
                ? R.string.entrenamiento_form_audio_grabada
                : R.string.entrenamiento_detalle_sin_audio);
        btnReproducir.setVisibility(hayAudio ? View.VISIBLE : View.GONE);
    }

    private void onReproducirClick() {
        if (TextUtils.isEmpty(audioPath)) {
            return;
        }
        if (reproduciendo) {
            detenerReproduccion();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.setOnCompletionListener(mp -> detenerReproduccion());
            mediaPlayer.prepare();
            mediaPlayer.start();

            reproduciendo = true;
            btnReproducir.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        } catch (IOException e) {
            Toast.makeText(this, R.string.entrenamiento_form_audio_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerReproduccion() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        reproduciendo = false;
        btnReproducir.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
    }

    private void onEliminarClick() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.entrenamiento_detalle_eliminar_confirmar_titulo)
                .setMessage(R.string.entrenamiento_detalle_eliminar_confirmar_mensaje)
                .setNegativeButton(R.string.entrenamiento_form_cancelar, null)
                .setPositiveButton(R.string.entrenamiento_detalle_eliminar, (dialog, which) -> eliminarEntrenamiento())
                .show();
    }

    private void eliminarEntrenamiento() {
        String entrenamientoId = getIntent().getStringExtra(EXTRA_ENTRENAMIENTO_ID);
        if (entrenamientoId == null) {
            return;
        }
        entrenamientoRepository.eliminar(entrenamientoId);
        Toast.makeText(this, R.string.entrenamiento_detalle_eliminado, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
