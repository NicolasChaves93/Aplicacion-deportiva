package com.entrenaapp.mobile.util;

import android.view.View;
import android.widget.TextView;

import com.entrenaapp.mobile.R;
import com.entrenaapp.mobile.data.local.managerdb.SyncStatus;

/**
 * Pinta el puntito + texto de "Sincronizado"/"Pendiente" que usan las
 * tarjetas de Deportistas y Entrenamientos, a partir del sync_status de la
 * fila local.
 */
public final class SyncStatusView {

    private SyncStatusView() {
        // impide crear objetos de esta clase
    }

    public static void pintar(View dot, TextView texto, String syncStatus) {
        boolean sincronizado = SyncStatus.SYNCED.equals(syncStatus);
        dot.setBackgroundResource(sincronizado ? R.drawable.bg_circle_success : R.drawable.bg_circle_outline);
        texto.setText(sincronizado ? R.string.sync_estado_sincronizado : R.string.sync_estado_pendiente);
    }
}
