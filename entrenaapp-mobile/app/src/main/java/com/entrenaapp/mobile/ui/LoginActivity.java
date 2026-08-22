package com.entrenaapp.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import java.util.Locale;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.entrenaapp.mobile.MainActivity;
import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.remote.model.AuthResponse;
import com.entrenaapp.mobile.data.remote.repository.AuthRepository;
import com.entrenaapp.mobile.data.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialCheckBox cbRecordar;
    private MaterialButton btnLogin;
    private ProgressBar progressLogin;
    private TextView tvError;

    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private Call<AuthResponse> currentCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.haySesionActiva()) {
            irAHome();
            return;
        }

        authRepository = new AuthRepository();
        initObjects();
        btnLogin.setOnClickListener(this::onLoginClick);
    }

    private void onLoginClick(View view) {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            mostrarError(getString(R.string.login_error_campos_vacios));
            return;
        }

        mostrarCargando();
        currentCall = authRepository.login(email, password);
        currentCall.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (isFinishing()) {
                    return;
                }
                ocultarCargando();

                AuthResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    if (cbRecordar.isChecked()) {
                        sessionManager.guardarSesion(body);
                    }
                    irAHome();
                } else if (response.code() == 401 || response.code() == 500) {
                    mostrarError(getString(R.string.login_error_credenciales));
                } else {
                    mostrarError(getString(R.string.login_error_conexion));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable throwable) {
                if (call.isCanceled() || isFinishing()) {
                    return;
                }
                ocultarCargando();
                mostrarError(getString(R.string.login_error_conexion));
            }
        });
    }

    private void irAHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void mostrarCargando() {
        btnLogin.setEnabled(false);
        progressLogin.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void ocultarCargando() {
        btnLogin.setEnabled(true);
        progressLogin.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        super.onDestroy();
    }

    private void initObjects() {
        this.etEmail = findViewById(R.id.etEmail);
        this.etPassword = findViewById(R.id.etPassword);
        this.cbRecordar = findViewById(R.id.cbRecordar);
        this.btnLogin = findViewById(R.id.btnLogin);
        this.progressLogin = findViewById(R.id.progressLogin);
        this.tvError = findViewById(R.id.tvError);
    }
}
