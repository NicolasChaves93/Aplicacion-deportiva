package com.entrenaapp.mobile.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

/**
 * Encapsula el ciclo de vida de un permiso peligroso: verificar si ya esta
 * concedido y, si no, solicitarlo en tiempo de ejecucion. Se instancia como
 * campo de la Activity/Fragment (antes de STARTED) para poder registrar el
 * ActivityResultLauncher, y desde ahi la UI solo llama a solicitar(callback).
 */
public class PermissionManager {

    public interface PermissionCallback {
        void onConcedido();

        void onDenegado();
    }

    private final Context context;
    private final String permiso;
    private final ActivityResultLauncher<String> launcher;
    private PermissionCallback callbackPendiente;

    public PermissionManager(ActivityResultCaller caller, Context context, String permiso) {
        this.context = context.getApplicationContext();
        this.permiso = permiso;
        this.launcher = caller.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (callbackPendiente == null) {
                        return;
                    }
                    PermissionCallback callback = callbackPendiente;
                    callbackPendiente = null;
                    if (concedido) {
                        callback.onConcedido();
                    } else {
                        callback.onDenegado();
                    }
                });
    }

    public boolean estaConcedido() {
        return ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED;
    }

    public void solicitar(PermissionCallback callback) {
        if (estaConcedido()) {
            callback.onConcedido();
            return;
        }
        this.callbackPendiente = callback;
        launcher.launch(permiso);
    }
}
