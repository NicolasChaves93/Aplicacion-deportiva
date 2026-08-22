package com.entrenaapp.mobile.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import com.entrenaapp.mobile.entities.Deportista;
import com.entrenaapp.mobile.managerdb.DeportistaContract;
import com.entrenaapp.mobile.managerdb.ManagerDataBase;
import com.entrenaapp.mobile.managerdb.SyncStatus;

public class DeportistaRepository {
    private static final String TAG = "DeportistaRepository";
    private final ManagerDataBase managerDataBase;

    public DeportistaRepository(Context context) {
        managerDataBase = new ManagerDataBase(context.getApplicationContext());
    }

    // Metodo crud para insertar un deportista en la bd local
    public String insertDeportista(Deportista deportista) {
        if (deportista.getId() == null || deportista.getId().isEmpty()) {
            deportista.setId(UUID.randomUUID().toString());
        }

        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_ID, deportista.getId());
        values.put(DeportistaContract.COLUMN_NOMBRE, deportista.getNombre());
        values.put(DeportistaContract.COLUMN_DOCUMENTO, deportista.getDocumento());
        values.put(DeportistaContract.COLUMN_EDAD, deportista.getEdad());
        values.put(DeportistaContract.COLUMN_DISCIPLINA, deportista.getDisciplina());
        values.put(DeportistaContract.COLUMN_FOTO_PATH, deportista.getFotoPath());
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            long row = database.insert(DeportistaContract.TABLE_NAME, null, values);
            return row != -1 ? deportista.getId() : null;
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar el deportista", e);
        }
        return null;
    }

    // Metodo para listar todos los deportistas
    public ArrayList<Deportista> obtenerTodos() {
        ArrayList<Deportista> lista = new ArrayList<>();
        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(DeportistaContract.TABLE_NAME,
                    null, null, null, null, null,
                    DeportistaContract.COLUMN_NOMBRE + " ASC")) {
                while (cursor.moveToNext()) {
                    lista.add(mapCursorToDeportista(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar los deportistas", e);
        }
        return lista;
    }

    // Metodo para buscar un deportista por su documento
    public Deportista obtenerPorDocumento(String documento) {
        String selection = DeportistaContract.COLUMN_DOCUMENTO + " = ?";
        String[] selectionArgs = {documento};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(DeportistaContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                if (cursor.moveToFirst()) {
                    return mapCursorToDeportista(cursor);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar el deportista", e);
        }
        return null;
    }

    // Metodo para actualizar los datos de un deportista existente
    public int actualizar(Deportista deportista) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_NOMBRE, deportista.getNombre());
        values.put(DeportistaContract.COLUMN_DOCUMENTO, deportista.getDocumento());
        values.put(DeportistaContract.COLUMN_EDAD, deportista.getEdad());
        values.put(DeportistaContract.COLUMN_DISCIPLINA, deportista.getDisciplina());
        values.put(DeportistaContract.COLUMN_FOTO_PATH, deportista.getFotoPath());
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {deportista.getId()};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.update(DeportistaContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar el deportista", e);
        }
        return -1;
    }

    // Metodo para eliminar un deportista de la bd local
    public int eliminar(String id) {
        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.delete(DeportistaContract.TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar el deportista", e);
        }
        return -1;
    }

    // Metodo para obtener los deportistas pendientes de sincronizar con la API
    public ArrayList<Deportista> obtenerPendientesSync() {
        ArrayList<Deportista> lista = new ArrayList<>();
        String selection = DeportistaContract.COLUMN_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.PENDING};

        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(DeportistaContract.TABLE_NAME,
                    null, selection, selectionArgs, null, null, null)) {
                while (cursor.moveToNext()) {
                    lista.add(mapCursorToDeportista(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar los deportistas pendientes de sincronizar", e);
        }
        return lista;
    }

    // Metodo para marcar un deportista como sincronizado con la API
    public int marcarComoSincronizado(String id) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            return database.update(DeportistaContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar el deportista como sincronizado", e);
        }
        return -1;
    }

    private Deportista mapCursorToDeportista(Cursor cursor) {
        Deportista deportista = new Deportista();
        deportista.setId(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_ID)));
        deportista.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_NOMBRE)));
        deportista.setDocumento(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_DOCUMENTO)));
        deportista.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_EDAD)));
        deportista.setDisciplina(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_DISCIPLINA)));
        deportista.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_FOTO_PATH)));
        deportista.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_SYNC_STATUS)));
        return deportista;
    }
}
