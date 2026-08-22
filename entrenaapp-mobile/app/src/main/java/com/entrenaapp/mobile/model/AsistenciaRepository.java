package com.entrenaapp.mobile.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import com.entrenaapp.mobile.entities.Asistencia;
import com.entrenaapp.mobile.managerdb.AsistenciaContract;
import com.entrenaapp.mobile.managerdb.ManagerDataBase;
import com.entrenaapp.mobile.managerdb.SyncStatus;

public class AsistenciaRepository {
    private static final String TAG = "AsistenciaRepository";
    private final ManagerDataBase managerDataBase;

    public AsistenciaRepository(Context context) {
        managerDataBase = new ManagerDataBase(context.getApplicationContext());
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
