package com.entrenaapp.mobile.ui.asistencia;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.util.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaAdapter extends RecyclerView.Adapter<AsistenciaAdapter.AsistenciaViewHolder> {

    public interface OnEstadoCambiadoListener {
        void onEstadoCambiado();
    }

    private final List<Deportista> deportistas = new ArrayList<>();
    // deportistaId -> true (presente) / false (ausente). Sin entrada = pendiente.
    private final Map<String, Boolean> estados = new HashMap<>();
    private final OnEstadoCambiadoListener listener;

    public AsistenciaAdapter(OnEstadoCambiadoListener listener) {
        this.listener = listener;
    }

    public void actualizarDatos(List<Deportista> nuevosDeportistas, Map<String, Boolean> estadosIniciales) {
        deportistas.clear();
        deportistas.addAll(nuevosDeportistas);
        estados.clear();
        estados.putAll(estadosIniciales);
        notifyDataSetChanged();
    }

    public Map<String, Boolean> getEstados() {
        return estados;
    }

    public int contarConfirmados() {
        return estados.size();
    }

    public int contarPresentes() {
        int presentes = 0;
        for (Boolean asistio : estados.values()) {
            if (Boolean.TRUE.equals(asistio)) {
                presentes++;
            }
        }
        return presentes;
    }

    public int contarAusentes() {
        return contarConfirmados() - contarPresentes();
    }

    public int contarTotal() {
        return deportistas.size();
    }

    @NonNull
    @Override
    public AsistenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asistencia, parent, false);
        return new AsistenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsistenciaViewHolder holder, int position) {
        holder.bind(deportistas.get(position));
    }

    @Override
    public int getItemCount() {
        return deportistas.size();
    }

    class AsistenciaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFoto;
        private final TextView tvNombre;
        private final TextView tvDisciplina;
        private final ImageButton btnPresente;
        private final ImageButton btnAusente;

        AsistenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDisciplina = itemView.findViewById(R.id.tvDisciplina);
            btnPresente = itemView.findViewById(R.id.btnPresente);
            btnAusente = itemView.findViewById(R.id.btnAusente);
        }

        void bind(Deportista deportista) {
            tvNombre.setText(deportista.getNombre());
            tvDisciplina.setText(deportista.getDisciplina());

            String fotoPath = deportista.getFotoPath();
            Bitmap bitmap = !TextUtils.isEmpty(fotoPath) && new File(fotoPath).exists()
                    ? ImageUtils.cargarBitmapRotado(fotoPath, 88, 88)
                    : null;
            if (bitmap != null) {
                ivFoto.setPadding(0, 0, 0, 0);
                ivFoto.setImageBitmap(bitmap);
            } else {
                ivFoto.setPadding(9, 9, 9, 9);
                ivFoto.setImageResource(R.drawable.ic_person_placeholder);
            }

            Boolean estado = estados.get(deportista.getId());
            pintarBoton(btnPresente, estado != null && estado, R.drawable.bg_circle_success);
            pintarBoton(btnAusente, estado != null && !estado, R.drawable.bg_circle_error);

            btnPresente.setOnClickListener(v -> cambiarEstado(deportista.getId(), Boolean.TRUE.equals(estado) ? null : true));
            btnAusente.setOnClickListener(v -> cambiarEstado(deportista.getId(), Boolean.FALSE.equals(estado) ? null : false));
        }

        private void pintarBoton(ImageButton boton, boolean activo, int fondoActivo) {
            boton.setBackgroundResource(activo ? fondoActivo : R.drawable.bg_circle_outline);
            boton.setColorFilter(itemView.getContext().getColor(activo ? android.R.color.white : R.color.brand_outline));
        }

        private void cambiarEstado(String deportistaId, Boolean nuevoEstado) {
            if (nuevoEstado == null) {
                estados.remove(deportistaId);
            } else {
                estados.put(deportistaId, nuevoEstado);
            }
            notifyItemChanged(getBindingAdapterPosition());
            listener.onEstadoCambiado();
        }
    }
}
