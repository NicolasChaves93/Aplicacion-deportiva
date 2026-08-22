package com.entrenaapp.mobile.data.sync;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Punto unico para programar la sincronizacion en segundo plano. Combina dos
 * disparadores para que sea "automatica" pero tambien demostrable al momento:
 *  - Un trabajo periodico de respaldo (cada 15 min, el minimo que permite
 *    WorkManager) por si el dispositivo pasa mucho tiempo sin nada nuevo.
 *  - Un trabajo unico que se programa cada vez que se guarda algo local
 *    (ver los Repository), que corre apenas hay conexion en vez de esperar
 *    al proximo ciclo periodico.
 * Ambos exigen NetworkType.CONNECTED: si no hay red, WorkManager los deja en
 * espera solo y los corre automaticamente en cuanto vuelve la conexion.
 */
public final class SyncScheduler {

    private static final String TRABAJO_PERIODICO = "sync_periodico";
    private static final String TRABAJO_INMEDIATO = "sync_inmediato";

    private SyncScheduler() {
        // impide crear objetos de esta clase
    }

    public static void programarSincronizacionPeriodica(Context context) {
        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest solicitud = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(restricciones)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(TRABAJO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP, solicitud);
    }

    // Se llama despues de cada guardado local (crear/editar/eliminar) para
    // que la subida ocurra lo antes posible en vez de esperar al ciclo
    // periodico de 15 minutos.
    public static void solicitarSincronizacion(Context context) {
        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest solicitud = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(restricciones)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(TRABAJO_INMEDIATO, ExistingWorkPolicy.REPLACE, solicitud);
    }
}
