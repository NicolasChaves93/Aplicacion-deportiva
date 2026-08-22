package com.entrenaapp.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entrenaapp.mobile.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITULO = "titulo";

    public PlaceholderFragment() {
        super(R.layout.fragment_placeholder);
    }

    public static PlaceholderFragment newInstance(String titulo) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, titulo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvTitulo = view.findViewById(R.id.tvPlaceholderTitulo);
        String titulo = getArguments() != null ? getArguments().getString(ARG_TITULO) : "";
        tvTitulo.setText(titulo);
    }
}
