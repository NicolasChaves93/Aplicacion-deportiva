package com.entrenaapp.mobile.ui.deportista;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.data.local.repository.DeportistaRepository;
import com.entrenaapp.mobile.util.ImageUtils;
import com.entrenaapp.mobile.util.PermissionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DeportistaFormActivity extends AppCompatActivity {

    private static final String EXTRA_DEPORTISTA_ID = "deportista_id";

    public static Intent crearIntentCrear(Context context) {
        return new Intent(context, DeportistaFormActivity.class);
    }

    public static Intent crearIntentEditar(Context context, String deportistaId) {
        Intent intent = new Intent(context, DeportistaFormActivity.class);
        intent.putExtra(EXTRA_DEPORTISTA_ID, deportistaId);
        return intent;
    }

    private TextView tvTitulo;
    private ImageView ivFoto;
    private TextInputEditText etNombre;
    private TextInputEditText etDocumento;
    private TextInputEditText etEdad;
    private TextInputEditText etDisciplina;
    private TextView tvError;
    private View cardZonaPeligro;

    private DeportistaRepository deportistaRepository;
    private PermissionManager cameraPermission;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    private File fotoFile;
    private Deportista deportistaEnEdicion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deportista_form);

        deportistaRepository = new DeportistaRepository(this);
        cameraPermission = new PermissionManager(this, this, Manifest.permission.CAMERA);
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), this::onFotoCapturada);

        initObjects();

        findViewById(R.id.fabCamara).setOnClickListener(v -> onCamaraClick());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> onGuardarClick());
        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());
        findViewById(R.id.btnEliminar).setOnClickListener(v -> onEliminarClick());

        String deportistaId = getIntent().getStringExtra(EXTRA_DEPORTISTA_ID);
        if (deportistaId != null) {
            cargarModoEdicion(deportistaId);
        }
    }

    private void cargarModoEdicion(String deportistaId) {
        deportistaEnEdicion = deportistaRepository.obtenerPorId(deportistaId);
        if (deportistaEnEdicion == null) {
            finish();
            return;
        }

        tvTitulo.setText(R.string.deportista_form_titulo_editar);
        ((android.widget.Button) findViewById(R.id.btnGuardar)).setText(R.string.deportista_form_guardar_cambios);
        cardZonaPeligro.setVisibility(View.VISIBLE);

        etNombre.setText(deportistaEnEdicion.getNombre());
        etDocumento.setText(deportistaEnEdicion.getDocumento());
        etEdad.setText(String.valueOf(deportistaEnEdicion.getEdad()));
        etDisciplina.setText(deportistaEnEdicion.getDisciplina());

        String fotoPath = deportistaEnEdicion.getFotoPath();
        if (!TextUtils.isEmpty(fotoPath) && new File(fotoPath).exists()) {
            Bitmap bitmap = ImageUtils.cargarBitmapRotado(fotoPath, 320, 320);
            if (bitmap != null) {
                ivFoto.setPadding(0, 0, 0, 0);
                ivFoto.setImageBitmap(bitmap);
            }
            fotoFile = new File(fotoPath);
        }
    }

    private void onCamaraClick() {
        cameraPermission.solicitar(new PermissionManager.PermissionCallback() {
            @Override
            public void onConcedido() {
                abrirCamara();
            }

            @Override
            public void onDenegado() {
                mostrarError(getString(R.string.deportista_form_error_camara));
            }
        });
    }

    private void abrirCamara() {
        try {
            fotoFile = crearArchivoFoto();
            Uri fotoUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", fotoFile);
            takePictureLauncher.launch(fotoUri);
        } catch (IOException e) {
            mostrarError(getString(R.string.deportista_form_error_camara));
        }
    }

    private File crearArchivoFoto() throws IOException {
        File dir = new File(getFilesDir(), "fotos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String marcaTiempo = LocalDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return new File(dir, "deportista_" + marcaTiempo + ".jpg");
    }

    private void onFotoCapturada(boolean exito) {
        if (!exito || fotoFile == null) {
            return;
        }
        Bitmap bitmap = ImageUtils.cargarBitmapRotado(fotoFile.getAbsolutePath(), 320, 320);
        if (bitmap != null) {
            ivFoto.setPadding(0, 0, 0, 0);
            ivFoto.setImageBitmap(bitmap);
        }
    }

    private void onGuardarClick() {
        String nombre = textoDe(etNombre);
        String documento = textoDe(etDocumento);
        String edadTexto = textoDe(etEdad);
        String disciplina = textoDe(etDisciplina);

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(documento)
                || TextUtils.isEmpty(edadTexto) || TextUtils.isEmpty(disciplina)) {
            mostrarError(getString(R.string.deportista_form_error_campos));
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            mostrarError(getString(R.string.deportista_form_error_edad));
            return;
        }

        String fotoPath = fotoFile != null ? fotoFile.getAbsolutePath() : null;
        int mensajeExito;

        if (deportistaEnEdicion == null) {
            Deportista nuevo = new Deportista(null, nombre, documento, edad, disciplina, fotoPath);
            DeportistaRepository.ResultadoGuardado resultado = deportistaRepository.crearOReactivar(nuevo);

            if (resultado == DeportistaRepository.ResultadoGuardado.DOCUMENTO_DUPLICADO) {
                mostrarError(getString(R.string.deportista_form_error_documento_duplicado));
                return;
            }
            mensajeExito = resultado == DeportistaRepository.ResultadoGuardado.REACTIVADO
                    ? R.string.deportista_form_reactivado
                    : R.string.deportista_form_guardado;
        } else {
            Deportista otro = deportistaRepository.obtenerPorDocumento(documento);
            if (otro != null && !otro.getId().equals(deportistaEnEdicion.getId())) {
                mostrarError(getString(R.string.deportista_form_error_documento_duplicado));
                return;
            }

            deportistaEnEdicion.setNombre(nombre);
            deportistaEnEdicion.setDocumento(documento);
            deportistaEnEdicion.setEdad(edad);
            deportistaEnEdicion.setDisciplina(disciplina);
            deportistaEnEdicion.setFotoPath(fotoPath);
            deportistaRepository.actualizar(deportistaEnEdicion);
            mensajeExito = R.string.deportista_form_guardado;
        }

        int duracionToast = mensajeExito == R.string.deportista_form_reactivado
                ? Toast.LENGTH_LONG
                : Toast.LENGTH_SHORT;
        Toast.makeText(this, mensajeExito, duracionToast).show();
        setResult(RESULT_OK);
        finish();
    }

    private void onEliminarClick() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.deportista_form_eliminar_confirmar_titulo)
                .setMessage(R.string.deportista_form_eliminar_confirmar_mensaje)
                .setNegativeButton(R.string.deportista_form_cancelar, null)
                .setPositiveButton(R.string.deportista_form_eliminar, (dialog, which) -> eliminarDeportista())
                .show();
    }

    private void eliminarDeportista() {
        if (deportistaEnEdicion == null) {
            return;
        }
        deportistaRepository.desactivar(deportistaEnEdicion.getId());
        Toast.makeText(this, R.string.deportista_form_eliminado, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private String textoDe(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    private void initObjects() {
        tvTitulo = findViewById(R.id.tvTitulo);
        ivFoto = findViewById(R.id.ivFoto);
        etNombre = findViewById(R.id.etNombre);
        etDocumento = findViewById(R.id.etDocumento);
        etEdad = findViewById(R.id.etEdad);
        etDisciplina = findViewById(R.id.etDisciplina);
        tvError = findViewById(R.id.tvError);
        cardZonaPeligro = findViewById(R.id.cardZonaPeligro);
    }
}
