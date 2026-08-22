package com.entrenaapp.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.session.SessionManager;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private SessionManager sessionManager;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        TextView tvBienvenida = view.findViewById(R.id.tvBienvenida);
        String nombre = sessionManager.getNombre();
        tvBienvenida.setText(getString(R.string.home_bienvenida, nombre != null ? nombre : ""));

        MaterialButton btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
