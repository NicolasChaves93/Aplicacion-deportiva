package com.entrenaapp.mobile.ui.entrenamiento;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.repository.EntrenamientoRepository;
import com.entrenaapp.mobile.util.ConnectivityObserver;
import com.entrenaapp.mobile.util.GeocodingUtils;
import com.entrenaapp.mobile.util.PermissionManager;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class EntrenamientoFormActivity extends AppCompatActivity {

    private TextInputEditText etFecha;
    private AutoCompleteTextView actvTipo;
    private TextInputEditText etDuracion;
    private SeekBar seekIntensidad;
    private TextView tvIntensidadValor;
    private TextView tvUbicacion;
    private TextView tvAudioEstado;
    private MaterialButton btnReproducir;
    private FloatingActionButton fabGrabar;
    private TextView tvError;

    private EntrenamientoRepository entrenamientoRepository;
    private PermissionManager locationPermission;
    private PermissionManager microphonePermission;
    private FusedLocationProviderClient locationClient;

    private Double latitud;
    private Double longitud;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean grabando;
    private boolean reproduciendo;
    private File audioFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento_form);

        entrenamientoRepository = new EntrenamientoRepository(this);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPermission = new PermissionManager(this, this, Manifest.permission.ACCESS_FINE_LOCATION);
        microphonePermission = new PermissionManager(this, this, Manifest.permission.RECORD_AUDIO);

        initObjects();
        configurarFecha();
        configurarTipo();
        configurarIntensidad();

        findViewById(R.id.btnActualizarUbicacion).setOnClickListener(v -> solicitarUbicacion());
        fabGrabar.setOnClickListener(v -> onGrabarClick());
        btnReproducir.setOnClickListener(v -> onReproducirClick());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> onGuardarClick());
        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());

        solicitarUbicacion();
    }

    private void configurarFecha() {
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        etFecha.setText(hoy.format(DateTimeFormatter.ISO_LOCAL_DATE));

        etFecha.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            android.app.DatePickerDialog dialogo = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                LocalDate fecha = LocalDate.of(year, month + 1, dayOfMonth);
                etFecha.setText(fecha.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
            // Un entrenamiento no puede registrarse en una fecha ya pasada.
            dialogo.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialogo.show();
        });
    }

    private void configurarTipo() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.entrenamiento_tipos, android.R.layout.simple_list_item_1);
        actvTipo.setAdapter(adapter);
        actvTipo.setOnClickListener(v -> actvTipo.showDropDown());
    }

    private void configurarIntensidad() {
        seekIntensidad.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvIntensidadValor.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no-op
            }
        });
    }

    private void solicitarUbicacion() {
        tvUbicacion.setText(R.string.entrenamiento_form_ubicacion_capturando);
        locationPermission.solicitar(new PermissionManager.PermissionCallback() {
            @Override
            public void onConcedido() {
                capturarUbicacion();
            }

            @Override
            public void onDenegado() {
                tvUbicacion.setText(R.string.entrenamiento_form_ubicacion_error_permiso);
            }
        });
    }

    @android.annotation.SuppressLint("MissingPermission")
    private void capturarUbicacion() {
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        locationClient.getCurrentLocation(request, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        tvUbicacion.setText(R.string.entrenamiento_form_ubicacion_error);
                        return;
                    }
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();
                    tvUbicacion.setText(getString(
                            R.string.entrenamiento_form_ubicacion_capturada, latitud, longitud));

                    GeocodingUtils.obtenerDireccion(this, latitud, longitud, direccion -> {
                        if (direccion != null) {
                            tvUbicacion.setText(direccion);
                        }
                    });
                })
                .addOnFailureListener(e -> tvUbicacion.setText(R.string.entrenamiento_form_ubicacion_error));
    }

    private void onGrabarClick() {
        if (grabando) {
            detenerGrabacion();
            return;
        }
        microphonePermission.solicitar(new PermissionManager.PermissionCallback() {
            @Override
            public void onConcedido() {
                iniciarGrabacion();
            }

            @Override
            public void onDenegado() {
                mostrarError(getString(R.string.entrenamiento_form_audio_error_permiso));
            }
        });
    }

    private void iniciarGrabacion() {
        try {
            audioFile = crearArchivoAudio();
            mediaRecorder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? new MediaRecorder(this)
                    : new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();

            grabando = true;
            fabGrabar.setImageResource(R.drawable.ic_stop);
            tvAudioEstado.setText(R.string.entrenamiento_form_audio_grabando);
            btnReproducir.setVisibility(View.GONE);
        } catch (IOException | IllegalStateException e) {
            mostrarError(getString(R.string.entrenamiento_form_audio_error));
            liberarGrabador();
        }
    }

    private void detenerGrabacion() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (IllegalStateException e) {
            audioFile = null;
        } finally {
            liberarGrabador();
        }

        grabando = false;
        fabGrabar.setImageResource(R.drawable.ic_mic);

        if (audioFile != null && audioFile.exists()) {
            tvAudioEstado.setText(R.string.entrenamiento_form_audio_grabada);
            btnReproducir.setVisibility(View.VISIBLE);
        } else {
            tvAudioEstado.setText(R.string.entrenamiento_form_audio_error);
        }
    }

    private void liberarGrabador() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private File crearArchivoAudio() throws IOException {
        File dir = new File(getFilesDir(), "audio");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String marcaTiempo = LocalDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return new File(dir, "entrenamiento_" + marcaTiempo + ".m4a");
    }

    private void onReproducirClick() {
        if (audioFile == null || !audioFile.exists()) {
            return;
        }
        if (reproduciendo) {
            detenerReproduccion();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.setOnCompletionListener(mp -> detenerReproduccion());
            mediaPlayer.prepare();
            mediaPlayer.start();

            reproduciendo = true;
            btnReproducir.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        } catch (IOException e) {
            mostrarError(getString(R.string.entrenamiento_form_audio_error));
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

    private void onGuardarClick() {
        String tipo = actvTipo.getText() != null ? actvTipo.getText().toString().trim() : "";
        String duracionTexto = etDuracion.getText() != null ? etDuracion.getText().toString().trim() : "";

        if (TextUtils.isEmpty(tipo) || TextUtils.isEmpty(duracionTexto)) {
            mostrarError(getString(R.string.entrenamiento_form_error_campos));
            return;
        }

        int duracion;
        try {
            duracion = Integer.parseInt(duracionTexto);
            if (duracion <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mostrarError(getString(R.string.entrenamiento_form_error_duracion));
            return;
        }

        String fecha = etFecha.getText() != null ? etFecha.getText().toString() : "";
        try {
            if (LocalDate.parse(fecha).isBefore(LocalDate.now(ZoneId.systemDefault()))) {
                mostrarError(getString(R.string.entrenamiento_form_error_fecha_pasada));
                return;
            }
        } catch (java.time.format.DateTimeParseException e) {
            mostrarError(getString(R.string.entrenamiento_form_error_campos));
            return;
        }

        String intensidad = tvIntensidadValor.getText().toString();
        String audioPath = audioFile != null && audioFile.exists() ? audioFile.getAbsolutePath() : null;

        Entrenamiento entrenamiento = new Entrenamiento(
                null, fecha, tipo, duracion, intensidad, latitud, longitud, audioPath);
        entrenamientoRepository.insertEntrenamiento(entrenamiento);

        int mensaje = ConnectivityObserver.hayConexion(this)
                ? R.string.entrenamiento_form_guardado
                : R.string.sync_guardado_offline;
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        liberarGrabador();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    private void initObjects() {
        etFecha = findViewById(R.id.etFecha);
        actvTipo = findViewById(R.id.actvTipo);
        etDuracion = findViewById(R.id.etDuracion);
        seekIntensidad = findViewById(R.id.seekIntensidad);
        tvIntensidadValor = findViewById(R.id.tvIntensidadValor);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvAudioEstado = findViewById(R.id.tvAudioEstado);
        btnReproducir = findViewById(R.id.btnReproducir);
        fabGrabar = findViewById(R.id.fabGrabar);
        tvError = findViewById(R.id.tvError);
    }
}
