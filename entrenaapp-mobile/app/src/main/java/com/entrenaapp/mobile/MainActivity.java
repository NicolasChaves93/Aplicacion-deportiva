package com.entrenaapp.mobile;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.entrenaapp.mobile.ui.HomeFragment;
import com.entrenaapp.mobile.ui.PlaceholderFragment;
import com.entrenaapp.mobile.ui.asistencia.AsistenciaFragment;
import com.entrenaapp.mobile.ui.deportista.DeportistasFragment;
import com.entrenaapp.mobile.ui.entrenamiento.EntrenamientosFragment;
import com.entrenaapp.mobile.util.ConnectivityObserver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TextView tvConexion;
    private android.view.View dotConexion;
    private ConnectivityManager.NetworkCallback connectivityCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initObjects();
        configurarBottomNavigation();
        actualizarIndicadorConexion(ConnectivityObserver.hayConexion(this));

        if (savedInstanceState == null) {
            cargarFragment(new HomeFragment());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Re-evalua el estado actual al volver a primer plano: si ya estaba
        // offline antes de salir (ej. a crear un deportista), el callback no
        // dispara nada nuevo porque no hay ningun "cambio" que reportar, y el
        // indicador se quedaria mostrando lo ultimo que tenia.
        actualizarIndicadorConexion(ConnectivityObserver.hayConexion(this));
        connectivityCallback = ConnectivityObserver.observar(this, this::actualizarIndicadorConexion);
    }

    @Override
    protected void onStop() {
        ConnectivityObserver.dejarDeObservar(this, connectivityCallback);
        super.onStop();
    }

    private void actualizarIndicadorConexion(boolean conectado) {
        runOnUiThread(() -> {
            tvConexion.setText(conectado ? R.string.conexion_en_linea : R.string.conexion_sin_conexion);
            dotConexion.setBackgroundResource(conectado ? R.drawable.bg_circle_success : R.drawable.bg_circle_error);
        });
    }

    private Fragment obtenerFragment(int itemId) {
        if (itemId == R.id.navigation_home) {
            return new HomeFragment();
        }
        if (itemId == R.id.navigation_athletes) {
            return new DeportistasFragment();
        }
        if (itemId == R.id.navigation_workouts) {
            return new EntrenamientosFragment();
        }
        if (itemId == R.id.navigation_attendance) {
            return new AsistenciaFragment();
        }
        if (itemId == R.id.navigation_settings) {
            return PlaceholderFragment.newInstance(getString(R.string.menu_settings) + " — " + getString(R.string.placeholder_proximamente));
        }
        return null;
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void configurarBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = obtenerFragment(item.getItemId());
            if (fragment == null) {
                return false;
            }
            cargarFragment(fragment);
            return true;
        });
    }

    private void initObjects() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvConexion = findViewById(R.id.tvConexion);
        dotConexion = findViewById(R.id.dotConexion);
    }
}
