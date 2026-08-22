package com.entrenaapp.mobile.data.local.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import com.entrenaapp.mobile.data.local.entities.Entrenamiento;
import com.entrenaapp.mobile.data.local.managerdb.EntrenamientoContract;
import com.entrenaapp.mobile.data.local.managerdb.ManagerDataBase;
import com.entrenaapp.mobile.data.local.managerdb.SyncStatus;

public class EntrenamientoRepository {
    private static final String TAG = "EntrenamientoRepository";
    private final ManagerDataBase managerDataBase;

    public EntrenamientoRepository(Context context) {
        managerDataBase = new ManagerDataBase(context.getApplicationContext());
    }

    // Metodo crud para insertar un entrenamiento en la bd local
    public String insertEntrenamiento(Entrenamiento entrenamiento) {
        if (entrenamiento.getId() == null || entrenamiento.getId().isEmpty()) {
            entrenamiento.setId(UUID.randomUUID().toString());
        }

        ContentValues values = new ContentValues();
        values.put(EntrenamientoContract.COLUMN_ID, entrenamiento.getId());
        values.put(EntrenamientoContract.COLUMN_FECHA, entrenamiento.getFecha());
        values.put(EntrenamientoContract.COLUMN_TIPO, entrenamiento.getTipo());
        values.put(EntrenamientoContract.COLUMN_DURACION_MIN, entrenamiento.getDuracionMin());
        values.put(EntrenamientoContract.COLUMN_INTENSIDAD, entrenamiento.getIntensidad());
        values.put(EntrenamientoContract.COLUMN_LATITUD, entrenamiento.getLatitud());
        values.put(EntrenamientoContract.COLUMN_LONGITUD, entrenamiento.getLongitud());
        values.put(EntrenamientoContract.COLUMN_AUDIO_PATH, entrenamiento.getAudioPath());
        values.put(EntrenamientoContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            long row = database.insert(EntrenamientoContract.TABLE_NAME, null, values);
            return row != -1 ? entrenamiento.getId() : null;
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar el entrenamiento", e);
        }
        return null;
    }

    // Metodo para listar todos los entrenamientos, del mas reciente al mas antiguo
    public ArrayList<Entrenamiento> obtenerTodos() {
        ArrayList<Entrenamiento> lista = new ArrayList<>();
        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(EntrenamientoContract.TABLE_NAME,
                    null, null, null, null, null,
                    EntrenamientoContract.COLUMN_FECHA + " DESC")) {
                while (cursor.moveToNext()) {
                    lista.add(mapCursorToEntrenamiento(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar los entrenamientos", e);
        }
        return lista;
    }

    // Metodo para buscar un entrenamiento por su id
    public Entrenamiento obtenerPorId(String id) {
        String selection = EntrenamientoContract.COLUMN_ID + " = ?";
        String[] selectionArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(EntrenamientoContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                if (cursor.moveToFirst()) {
                    return mapCursorToEntrenamiento(cursor);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar el entrenamiento", e);
        }
        return null;
    }

    // Metodo para eliminar un entrenamiento (elimina en cascada sus asistencias)
    public int eliminar(String id) {
        String whereClause = EntrenamientoContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.delete(EntrenamientoContract.TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar el entrenamiento", e);
        }
        return -1;
    }

    // Metodo para obtener los entrenamientos pendientes de sincronizar con la API
    public ArrayList<Entrenamiento> obtenerPendientesSync() {
        ArrayList<Entrenamiento> lista = new ArrayList<>();
        String selection = EntrenamientoContract.COLUMN_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.PENDING};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(EntrenamientoContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                while (cursor.moveToNext()) {
                    lista.add(mapCursorToEntrenamiento(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar los entrenamientos pendientes de sincronizar", e);
        }
        return lista;
    }

    // Metodo para marcar un entrenamiento como sincronizado con la API
    public int marcarComoSincronizado(String id) {
        ContentValues values = new ContentValues();
        values.put(EntrenamientoContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        String whereClause = EntrenamientoContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.update(EntrenamientoContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar el entrenamiento como sincronizado", e);
        }
        return -1;
    }

    private Entrenamiento mapCursorToEntrenamiento(Cursor cursor) {
        Entrenamiento entrenamiento = new Entrenamiento();
        entrenamiento.setId(cursor.getString(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_ID)));
        entrenamiento.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_FECHA)));
        entrenamiento.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_TIPO)));
        entrenamiento.setDuracionMin(cursor.getInt(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_DURACION_MIN)));
        entrenamiento.setIntensidad(cursor.getString(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_INTENSIDAD)));

        int latIndex = cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_LATITUD);
        entrenamiento.setLatitud(cursor.isNull(latIndex) ? null : cursor.getDouble(latIndex));

        int lngIndex = cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_LONGITUD);
        entrenamiento.setLongitud(cursor.isNull(lngIndex) ? null : cursor.getDouble(lngIndex));

        entrenamiento.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_AUDIO_PATH)));
        entrenamiento.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(EntrenamientoContract.COLUMN_SYNC_STATUS)));
        return entrenamiento;
    }
}
