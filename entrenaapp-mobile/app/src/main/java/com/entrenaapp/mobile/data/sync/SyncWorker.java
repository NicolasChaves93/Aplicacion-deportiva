package com.entrenaapp.mobile.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.entrenaapp.mobile.data.remote.repository.SyncRepository;

/**
 * Corre en segundo plano (WorkManager) solo cuando hay red disponible (ver
 * SyncScheduler). Sube Deportistas, Entrenamientos y Asistencias pendientes.
 */
public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SyncRepository syncRepository = new SyncRepository(getApplicationContext());
        boolean exito = syncRepository.sincronizarTodo();
        return exito ? Result.success() : Result.retry();
    }
}
