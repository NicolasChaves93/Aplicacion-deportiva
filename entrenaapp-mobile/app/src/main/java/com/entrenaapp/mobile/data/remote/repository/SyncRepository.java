package com.entrenaapp.mobile.data.remote.repository;

import android.content.Context;
import android.util.Log;

import com.entrenaapp.mobile.data.local.entities.Asistencia;
import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.managerdb.SyncStatus;
import com.entrenaapp.mobile.data.local.repository.AsistenciaRepository;
import com.entrenaapp.mobile.data.local.repository.DeportistaRepository;
import com.entrenaapp.mobile.data.local.repository.EntrenamientoRepository;
import com.entrenaapp.mobile.data.remote.RetrofitClient;
import com.entrenaapp.mobile.data.remote.SyncApiService;
import com.entrenaapp.mobile.data.remote.model.AsistenciaSyncRequest;
import com.entrenaapp.mobile.data.remote.model.AsistenciaSyncResponse;
import com.entrenaapp.mobile.data.remote.model.DeportistaSyncRequest;
import com.entrenaapp.mobile.data.remote.model.DeportistaSyncResponse;
import com.entrenaapp.mobile.data.remote.model.EntrenamientoSyncRequest;
import com.entrenaapp.mobile.data.remote.model.EntrenamientoSyncResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Response;

/**
 * Sincroniza SQLite con la API en las dos direcciones:
 *  - Bajada (API -> celular): reconcilia cambios hechos del lado servidor
 *    que la app nunca empujo (ej. alguien edito o borro un registro directo
 *    en la base de datos). Deportistas y Entrenamientos usan su "listar
 *    todos"; Asistencia no tiene ese endpoint, asi que se consulta por cada
 *    Entrenamiento local (GET /asistencias/entrenamiento/{id}).
 *  - Subida (celular -> API): lo que quedo pendiente en SQLite.
 *
 * Orden fijo: primero se baja (para no pisar con un push algo que ya
 * cambio del lado servidor), y en la subida, Deportistas y Entrenamientos
 * antes que Asistencias, por las FOREIGN KEY del lado servidor.
 */
public class SyncRepository {
    private static final String TAG = "SyncRepository";

    private final DeportistaRepository deportistaRepository;
    private final EntrenamientoRepository entrenamientoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final SyncApiService syncApiService;

    public SyncRepository(Context context) {
        deportistaRepository = new DeportistaRepository(context);
        entrenamientoRepository = new EntrenamientoRepository(context);
        asistenciaRepository = new AsistenciaRepository(context);
        syncApiService = RetrofitClient.getSyncService(context);
    }

    /**
     * Devuelve true si todo (bajada + subida) se completo sin errores. Si
     * algo fallo (ej. se corto la red a mitad de camino), devuelve false
     * para que el llamador reintente mas tarde; lo que si se alcanzo a
     * procesar queda aplicado.
     */
    public boolean sincronizarTodo() {
        boolean exito = true;
        exito &= descargarDeportistas();
        exito &= descargarEntrenamientos();
        exito &= descargarAsistencias();
        exito &= subirDeportistas();
        exito &= subirEntrenamientos();
        exito &= subirAsistencias();
        return exito;
    }

    // ---------- Bajada ----------

    private boolean descargarDeportistas() {
        List<DeportistaSyncResponse> remotos = obtenerCuerpo(syncApiService.obtenerDeportistas(), "listar deportistas");
        if (remotos == null) {
            return false;
        }

        Set<String> idsActivosRemotos = new HashSet<>();
        for (DeportistaSyncResponse remoto : remotos) {
            idsActivosRemotos.add(remoto.getId());
            aplicarDeportistaRemoto(remoto);
        }

        // Los deportistas activos y ya confirmados en el servidor que dejaron
        // de aparecer en la lista de activos pudieron desactivarse o borrarse
        // directo en la base: hay que confirmar cual de los dos paso.
        for (Deportista local : deportistaRepository.obtenerTodos()) {
            if (SyncStatus.SYNCED.equals(local.getSyncStatus()) && !idsActivosRemotos.contains(local.getId())) {
                reconciliarDeportistaAusente(local.getId());
            }
        }
        return true;
    }

    private void aplicarDeportistaRemoto(DeportistaSyncResponse remoto) {
        Deportista local = deportistaRepository.obtenerPorId(remoto.getId());
        Deportista mapeado = new Deportista(
                remoto.getId(), remoto.getNombre(), remoto.getDocumento(), remoto.getEdad(),
                remoto.getDisciplina(), remoto.getFotoPath());
        mapeado.setActivo(Boolean.TRUE.equals(remoto.getActivo()));

        if (local == null) {
            deportistaRepository.insertDesdeServidor(mapeado);
        } else if (SyncStatus.SYNCED.equals(local.getSyncStatus())) {
            // Si esta PENDING, hay una edicion local sin subir todavia: no se
            // pisa con lo remoto, se deja que la fase de subida la mande.
            deportistaRepository.actualizarDesdeServidor(mapeado);
        }
    }

    private void reconciliarDeportistaAusente(String id) {
        try {
            Response<DeportistaSyncResponse> response = syncApiService.obtenerDeportistaPorId(id).execute();
            if (response.isSuccessful() && response.body() != null) {
                if (Boolean.FALSE.equals(response.body().getActivo())) {
                    deportistaRepository.desactivarDesdeServidor(id);
                }
            } else if (response.code() == 404) {
                // Ya no existe en absoluto: se borro directo en la base.
                deportistaRepository.eliminarLocalSolo(id);
            }
        } catch (IOException e) {
            Log.w(TAG, "Sin conexion al confirmar el estado remoto del deportista " + id, e);
        }
    }

    private boolean descargarEntrenamientos() {
        List<EntrenamientoSyncResponse> remotos = obtenerCuerpo(syncApiService.obtenerEntrenamientos(), "listar entrenamientos");
        if (remotos == null) {
            return false;
        }

        Set<String> idsRemotos = new HashSet<>();
        for (EntrenamientoSyncResponse remoto : remotos) {
            idsRemotos.add(remoto.getId());
            aplicarEntrenamientoRemoto(remoto);
        }

        // A diferencia de Deportista, aqui no hay soft-delete: si un
        // entrenamiento ya confirmado en el servidor desaparecio de la
        // lista, es porque se borro directo en la base.
        for (Entrenamiento local : entrenamientoRepository.obtenerTodos()) {
            if (SyncStatus.SYNCED.equals(local.getSyncStatus()) && !idsRemotos.contains(local.getId())) {
                entrenamientoRepository.eliminarLocalSolo(local.getId());
            }
        }
        return true;
    }

    private void aplicarEntrenamientoRemoto(EntrenamientoSyncResponse remoto) {
        Entrenamiento local = entrenamientoRepository.obtenerPorId(remoto.getId());
        Entrenamiento mapeado = new Entrenamiento(
                remoto.getId(), remoto.getFecha(), remoto.getTipo(), remoto.getDuracionMin(),
                remoto.getIntensidad(), remoto.getLatitud(), remoto.getLongitud(),
                remoto.getObservacionAudioPath());

        if (local == null) {
            entrenamientoRepository.insertDesdeServidor(mapeado);
        } else if (SyncStatus.SYNCED.equals(local.getSyncStatus())) {
            entrenamientoRepository.actualizarDesdeServidor(mapeado);
        }
    }

    // Asistencia no tiene "listar todos" en la API: se consulta por cada
    // Entrenamiento local (ya reconciliado por descargarEntrenamientos, que
    // corre justo antes). El costo esta acotado por la cantidad de
    // entrenamientos que haya en el dispositivo.
    private boolean descargarAsistencias() {
        boolean exito = true;
        for (Entrenamiento entrenamiento : entrenamientoRepository.obtenerTodos()) {
            exito &= descargarAsistenciasDe(entrenamiento.getId());
        }
        return exito;
    }

    private boolean descargarAsistenciasDe(String entrenamientoId) {
        List<AsistenciaSyncResponse> remotas = obtenerCuerpo(
                syncApiService.obtenerAsistenciasPorEntrenamiento(entrenamientoId),
                "listar asistencias del entrenamiento " + entrenamientoId);
        if (remotas == null) {
            return false;
        }

        Set<String> idsRemotos = new HashSet<>();
        for (AsistenciaSyncResponse remota : remotas) {
            idsRemotos.add(remota.getId());
            aplicarAsistenciaRemota(remota);
        }

        for (Asistencia local : asistenciaRepository.obtenerPorEntrenamiento(entrenamientoId)) {
            if (SyncStatus.SYNCED.equals(local.getSyncStatus()) && !idsRemotos.contains(local.getId())) {
                asistenciaRepository.eliminarLocalSolo(local.getId());
            }
        }
        return true;
    }

    private void aplicarAsistenciaRemota(AsistenciaSyncResponse remota) {
        Asistencia local = asistenciaRepository.obtenerPorId(remota.getId());
        Asistencia mapeada = new Asistencia(
                remota.getId(), remota.getEntrenamientoId(), remota.getDeportistaId(),
                Boolean.TRUE.equals(remota.getAsistio()), remota.getObservacion());

        if (local == null) {
            asistenciaRepository.insertDesdeServidor(mapeada);
        } else if (SyncStatus.SYNCED.equals(local.getSyncStatus())) {
            asistenciaRepository.actualizarDesdeServidor(mapeada);
        }
    }

    // ---------- Subida ----------

    private boolean subirDeportistas() {
        boolean exito = true;
        List<Deportista> pendientes = deportistaRepository.obtenerPendientesSync();
        for (Deportista deportista : pendientes) {
            DeportistaSyncRequest request = new DeportistaSyncRequest(
                    deportista.getId(), deportista.getNombre(), deportista.getDocumento(),
                    deportista.getEdad(), deportista.getDisciplina(), deportista.getFotoPath());

            if (ejecutar(() -> syncApiService.sincronizarDeportista(request), "deportista " + deportista.getId())) {
                deportistaRepository.marcarComoSincronizado(deportista.getId());
            } else {
                exito = false;
            }
        }
        return exito;
    }

    private boolean subirEntrenamientos() {
        boolean exito = true;
        List<Entrenamiento> pendientes = entrenamientoRepository.obtenerPendientesSync();
        for (Entrenamiento entrenamiento : pendientes) {
            EntrenamientoSyncRequest request = new EntrenamientoSyncRequest(
                    entrenamiento.getId(), entrenamiento.getFecha(), entrenamiento.getTipo(),
                    entrenamiento.getDuracionMin(), entrenamiento.getIntensidad(),
                    entrenamiento.getLatitud(), entrenamiento.getLongitud(), entrenamiento.getAudioPath());

            if (ejecutar(() -> syncApiService.sincronizarEntrenamiento(request), "entrenamiento " + entrenamiento.getId())) {
                entrenamientoRepository.marcarComoSincronizado(entrenamiento.getId());
            } else {
                exito = false;
            }
        }
        return exito;
    }

    private boolean subirAsistencias() {
        boolean exito = true;
        List<Asistencia> pendientes = asistenciaRepository.obtenerPendientesSync();
        for (Asistencia asistencia : pendientes) {
            AsistenciaSyncRequest request = new AsistenciaSyncRequest(
                    asistencia.getId(), asistencia.getEntrenamientoId(), asistencia.getDeportistaId(),
                    asistencia.isAsistio(), asistencia.getObservacion());

            if (ejecutar(() -> syncApiService.sincronizarAsistencia(request), "asistencia " + asistencia.getId())) {
                asistenciaRepository.marcarComoSincronizado(asistencia.getId());
            } else {
                exito = false;
            }
        }
        return exito;
    }

    // ---------- Helpers ----------

    private <T> T obtenerCuerpo(retrofit2.Call<T> llamada, String descripcion) {
        try {
            Response<T> response = llamada.execute();
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Fallo al " + descripcion + ": HTTP " + response.code());
                return null;
            }
            return response.body();
        } catch (IOException e) {
            Log.w(TAG, "Sin conexion al " + descripcion, e);
            return null;
        }
    }

    private boolean ejecutar(LlamadaRetrofit llamada, String descripcion) {
        try {
            Response<Void> response = llamada.invocar().execute();
            if (!response.isSuccessful()) {
                Log.w(TAG, "Fallo al sincronizar " + descripcion + ": HTTP " + response.code());
            }
            return response.isSuccessful();
        } catch (IOException e) {
            Log.w(TAG, "Sin conexion al sincronizar " + descripcion, e);
            return false;
        }
    }

    private interface LlamadaRetrofit {
        retrofit2.Call<Void> invocar();
    }
}
