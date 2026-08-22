package com.entrenaapp.mobile;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.entrenaapp.mobile.ui.HomeFragment;
import com.entrenaapp.mobile.ui.PlaceholderFragment;
import com.entrenaapp.mobile.ui.deportista.DeportistasFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

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

        if (savedInstanceState == null) {
            cargarFragment(new HomeFragment());
        }
    }

    private Fragment obtenerFragment(int itemId) {
        if (itemId == R.id.navigation_home) {
            return new HomeFragment();
        }
        if (itemId == R.id.navigation_athletes) {
            return new DeportistasFragment();
        }
        if (itemId == R.id.navigation_workouts) {
            return PlaceholderFragment.newInstance(getString(R.string.menu_workouts) + " — " + getString(R.string.placeholder_proximamente));
        }
        if (itemId == R.id.navigation_attendance) {
            return PlaceholderFragment.newInstance(getString(R.string.menu_attendance) + " — " + getString(R.string.placeholder_proximamente));
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
    }
}
