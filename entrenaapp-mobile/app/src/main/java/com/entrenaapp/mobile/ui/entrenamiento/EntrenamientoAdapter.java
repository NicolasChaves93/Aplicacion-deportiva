package com.entrenaapp.mobile.ui.entrenamiento;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.util.SyncStatusView;

import java.util.ArrayList;
import java.util.List;

public class EntrenamientoAdapter extends RecyclerView.Adapter<EntrenamientoAdapter.EntrenamientoViewHolder> {

    public interface OnEntrenamientoClickListener {
        void onEntrenamientoClick(Entrenamiento entrenamiento);
    }

    private final List<Entrenamiento> entrenamientos = new ArrayList<>();
    private final OnEntrenamientoClickListener listener;

    public EntrenamientoAdapter(OnEntrenamientoClickListener listener) {
        this.listener = listener;
    }

    public void actualizarDatos(List<Entrenamiento> nuevosEntrenamientos) {
        entrenamientos.clear();
        if (nuevosEntrenamientos != null) {
            entrenamientos.addAll(nuevosEntrenamientos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntrenamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrenamiento, parent, false);
        return new EntrenamientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrenamientoViewHolder holder, int position) {
        holder.bind(entrenamientos.get(position));
    }

    @Override
    public int getItemCount() {
        return entrenamientos.size();
    }

    class EntrenamientoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTipo;
        private final TextView tvFecha;
        private final ImageView ivAudio;
        private final View dotSync;
        private final TextView tvSyncStatus;

        EntrenamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            ivAudio = itemView.findViewById(R.id.ivAudio);
            dotSync = itemView.findViewById(R.id.dotSync);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
        }

        void bind(Entrenamiento entrenamiento) {
            tvTipo.setText(entrenamiento.getTipo());
            String intensidad = itemView.getContext().getString(
                    R.string.entrenamiento_detalle_intensidad, entrenamiento.getIntensidad());
            tvFecha.setText(entrenamiento.getFecha() + " · " + entrenamiento.getDuracionMin() + " min · " + intensidad);
            SyncStatusView.pintar(dotSync, tvSyncStatus, entrenamiento.getSyncStatus());

            ivAudio.setVisibility(!TextUtils.isEmpty(entrenamiento.getAudioPath()) ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(view -> listener.onEntrenamientoClick(entrenamiento));
        }
    }
}
