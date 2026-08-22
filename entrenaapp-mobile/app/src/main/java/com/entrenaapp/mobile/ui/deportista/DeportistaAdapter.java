package com.entrenaapp.mobile.ui.deportista;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.util.ImageUtils;
import com.entrenaapp.mobile.util.SyncStatusView;

import java.util.ArrayList;
import java.util.List;

public class DeportistaAdapter extends RecyclerView.Adapter<DeportistaAdapter.DeportistaViewHolder> {

    public interface OnDeportistaClickListener {
        void onDeportistaClick(Deportista deportista);
    }

    private final List<Deportista> deportistas = new ArrayList<>();
    private final OnDeportistaClickListener listener;

    public DeportistaAdapter(OnDeportistaClickListener listener) {
        this.listener = listener;
    }

    public void actualizarDatos(List<Deportista> nuevosDeportistas) {
        deportistas.clear();
        if (nuevosDeportistas != null) {
            deportistas.addAll(nuevosDeportistas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeportistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deportista, parent, false);
        return new DeportistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeportistaViewHolder holder, int position) {
        holder.bind(deportistas.get(position));
    }

    @Override
    public int getItemCount() {
        return deportistas.size();
    }

    class DeportistaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFoto;
        private final TextView tvNombre;
        private final TextView tvDisciplina;
        private final TextView tvDocumento;
        private final View dotSync;
        private final TextView tvSyncStatus;

        DeportistaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDisciplina = itemView.findViewById(R.id.tvDisciplina);
            tvDocumento = itemView.findViewById(R.id.tvDocumento);
            dotSync = itemView.findViewById(R.id.dotSync);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
        }

        void bind(Deportista deportista) {
            tvNombre.setText(deportista.getNombre());
            tvDisciplina.setText(deportista.getDisciplina());
            tvDocumento.setText(itemView.getContext()
                    .getString(R.string.deportista_form_documento) + ": " + deportista.getDocumento());
            SyncStatusView.pintar(dotSync, tvSyncStatus, deportista.getSyncStatus());

            String fotoPath = deportista.getFotoPath();
            Bitmap bitmap = !TextUtils.isEmpty(fotoPath) && new File(fotoPath).exists()
                    ? ImageUtils.cargarBitmapRotado(fotoPath, 112, 112)
                    : null;

            if (bitmap != null) {
                ivFoto.setPadding(0, 0, 0, 0);
                ivFoto.setImageBitmap(bitmap);
            } else {
                ivFoto.setPadding(12, 12, 12, 12);
                ivFoto.setImageResource(R.drawable.ic_person_placeholder);
            }

            itemView.setOnClickListener(view -> listener.onDeportistaClick(deportista));
        }
    }
}
