package com.entrenaapp.mobile.data.local.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import com.entrenaapp.mobile.data.local.entities.Asistencia;
import com.entrenaapp.mobile.data.local.managerdb.AsistenciaContract;
import com.entrenaapp.mobile.data.local.managerdb.ManagerDataBase;
import com.entrenaapp.mobile.data.local.managerdb.SyncStatus;
import com.entrenaapp.mobile.data.sync.SyncScheduler;

public class AsistenciaRepository {
    private static final String TAG = "AsistenciaRepository";
    private final ManagerDataBase managerDataBase;
    private final Context context;

    public AsistenciaRepository(Context context) {
        this.context = context.getApplicationContext();
        managerDataBase = new ManagerDataBase(this.context);
    }

    // Metodo crud para registrar la asistencia de un deportista a un entrenamiento
    public String insertAsistencia(Asistencia asistencia) {
        if (asistencia.getId() == null || asistencia.getId().isEmpty()) {
            asistencia.setId(UUID.randomUUID().toString());
        }

        ContentValues values = new ContentValues();
        values.put(AsistenciaContract.COLUMN_ID, asistencia.getId());
        values.put(AsistenciaContract.COLUMN_ENTRENAMIENTO_ID, asistencia.getEntrenamientoId());
        values.put(AsistenciaContract.COLUMN_DEPORTISTA_ID, asistencia.getDeportistaId());
        values.put(AsistenciaContract.COLUMN_ASISTIO, asistencia.isAsistio() ? 1 : 0);
        values.put(AsistenciaContract.COLUMN_OBSERVACION, asistencia.getObservacion());
        values.put(AsistenciaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            long row = database.insert(AsistenciaContract.TABLE_NAME, null, values);
            SyncScheduler.solicitarSincronizacion(context);
            return row != -1 ? asistencia.getId() : null;
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar la asistencia", e);
        }
        return null;
    }

    // Metodo para listar las asistencias de un entrenamiento especifico
    public ArrayList<Asistencia> obtenerPorEntrenamiento(String entrenamientoId) {
        ArrayList<Asistencia> lista = new ArrayList<>();
        String selection = AsistenciaContract.COLUMN_ENTRENAMIENTO_ID + " = ?";
        String[] selectionArgs = {entrenamientoId};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(AsistenciaContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                while (cursor.moveToNext()) {
                    lista.add(mapCursorToAsistencia(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar las asistencias del entrenamiento", e);
        }
        return lista;
    }

    // Metodo para buscar la asistencia de un deportista en un entrenamiento
    // especifico (para saber si hay que insertar o actualizar)
    public Asistencia obtenerPorEntrenamientoYDeportista(String entrenamientoId, String deportistaId) {
        String selection = AsistenciaContract.COLUMN_ENTRENAMIENTO_ID + " = ? AND "
                + AsistenciaContract.COLUMN_DEPORTISTA_ID + " = ?";
        String[] selectionArgs = {entrenamientoId, deportistaId};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(AsistenciaContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                if (cursor.moveToFirst()) {
                    return mapCursorToAsistencia(cursor);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar la asistencia", e);
        }
        return null;
    }

    // Inserta la asistencia si es la primera vez que se marca para ese
    // deportista en ese entrenamiento, o actualiza el registro existente
    public void guardarAsistencia(Asistencia asistencia) {
        Asistencia existente = obtenerPorEntrenamientoYDeportista(
                asistencia.getEntrenamientoId(), asistencia.getDeportistaId());

        if (existente == null) {
            insertAsistencia(asistencia);
            return;
        }

        asistencia.setId(existente.getId());
        ContentValues values = new ContentValues();
        values.put(AsistenciaContract.COLUMN_ASISTIO, asistencia.isAsistio() ? 1 : 0);
        values.put(AsistenciaContract.COLUMN_OBSERVACION, asistencia.getObservacion());
        values.put(AsistenciaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        String whereClause = AsistenciaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {existente.getId()};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.update(AsistenciaContract.TABLE_NAME, values, whereClause, whereArgs);
            SyncScheduler.solicitarSincronizacion(context);
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar la asistencia", e);
        }
    }

    // Metodo para buscar una asistencia por su id
    public Asistencia obtenerPorId(String id) {
        String selection = AsistenciaContract.COLUMN_ID + " = ?";
        String[] selectionArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(AsistenciaContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                if (cursor.moveToFirst()) {
                    return mapCursorToAsistencia(cursor);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar la asistencia por id", e);
        }
        return null;
    }

    // --- Sincronizacion de bajada (mismo patron que Deportista/Entrenamiento) ---

    public void insertDesdeServidor(Asistencia asistencia) {
        ContentValues values = new ContentValues();
        values.put(AsistenciaContract.COLUMN_ID, asistencia.getId());
        values.put(AsistenciaContract.COLUMN_ENTRENAMIENTO_ID, asistencia.getEntrenamientoId());
        values.put(AsistenciaContract.COLUMN_DEPORTISTA_ID, asistencia.getDeportistaId());
        values.put(AsistenciaContract.COLUMN_ASISTIO, asistencia.isAsistio() ? 1 : 0);
        values.put(AsistenciaContract.COLUMN_OBSERVACION, asistencia.getObservacion());
        values.put(AsistenciaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.insert(AsistenciaContract.TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar la asistencia bajada del servidor", e);
        }
    }

    public void actualizarDesdeServidor(Asistencia asistencia) {
        ContentValues values = new ContentValues();
        values.put(AsistenciaContract.COLUMN_ASISTIO, asistencia.isAsistio() ? 1 : 0);
        values.put(AsistenciaContract.COLUMN_OBSERVACION, asistencia.getObservacion());
        values.put(AsistenciaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        String whereClause = AsistenciaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {asistencia.getId()};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.update(AsistenciaContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar la asistencia bajada del servidor", e);
        }
    }

    // Borrado fisico local: lo usa la reconciliacion de bajada cuando una
    // asistencia que estaba sincronizada ya no aparece en la API para ese
    // entrenamiento.
    public void eliminarLocalSolo(String id) {
        String whereClause = AsistenciaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.delete(AsistenciaContract.TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar localmente la asistencia (bajada del servidor)", e);
        }
    }

    // Metodo para eliminar un registro de asistencia
    public int eliminar(String id) {
        String whereClause = AsistenciaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.delete(AsistenciaContract.TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar la asistencia", e);
        }
        return -1;
    }

    // Metodo para obtener las asistencias pendientes de sincronizar con la API
    public ArrayList<Asistencia> obtenerPendientesSync() {
        ArrayList<Asistencia> lista = new ArrayList<>();
        String selection = AsistenciaContract.COLUMN_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.PENDING};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(AsistenciaContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                while (cursor.moveToNext()) {
                    lista.add(mapCursorToAsistencia(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar las asistencias pendientes de sincronizar", e);
        }
        return lista;
    }

    // Metodo para marcar una asistencia como sincronizada con la API
    public int marcarComoSincronizado(String id) {
        ContentValues values = new ContentValues();
        values.put(AsistenciaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        String whereClause = AsistenciaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.update(AsistenciaContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar la asistencia como sincronizada", e);
        }
        return -1;
    }

    private Asistencia mapCursorToAsistencia(Cursor cursor) {
        Asistencia asistencia = new Asistencia();
        asistencia.setId(cursor.getString(cursor.getColumnIndexOrThrow(AsistenciaContract.COLUMN_ID)));
        asistencia.setEntrenamientoId(cursor.getString(cursor.getColumnIndexOrThrow(AsistenciaContract.COLUMN_ENTRENAMIENTO_ID)));
        asistencia.setDeportistaId(cursor.getString(cursor.getColumnIndexOrThrow(AsistenciaContract.COLUMN_DEPORTISTA_ID)));
        asistencia.setAsistio(cursor.getInt(cursor.getColumnIndexOrThrow(AsistenciaContract.COLUMN_ASISTIO)) == 1);
        asistencia.setObservacion(cursor.getString(cursor.getColumnIndexOrThrow(AsistenciaContract.COLUMN_OBSERVACION)));
        asistencia.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(AsistenciaContract.COLUMN_SYNC_STATUS)));
        return asistencia;
    }
}
