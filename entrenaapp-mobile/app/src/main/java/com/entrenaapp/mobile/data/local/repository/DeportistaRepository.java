package com.entrenaapp.mobile.data.local.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import com.entrenaapp.mobile.data.local.entities.Deportista;
import com.entrenaapp.mobile.data.local.managerdb.DeportistaContract;
import com.entrenaapp.mobile.data.local.managerdb.ManagerDataBase;
import com.entrenaapp.mobile.data.local.managerdb.SyncStatus;
import com.entrenaapp.mobile.data.sync.SyncScheduler;

public class DeportistaRepository {
    private static final String TAG = "DeportistaRepository";
    private final ManagerDataBase managerDataBase;
    private final Context context;

    public DeportistaRepository(Context context) {
        this.context = context.getApplicationContext();
        managerDataBase = new ManagerDataBase(this.context);
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
        values.put(DeportistaContract.COLUMN_ACTIVO, 1);
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            long row = database.insert(DeportistaContract.TABLE_NAME, null, values);
            SyncScheduler.solicitarSincronizacion(context);
            return row != -1 ? deportista.getId() : null;
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar el deportista", e);
        }
        return null;
    }

    // Metodo para listar los deportistas activos (excluye los soft-eliminados)
    public ArrayList<Deportista> obtenerTodos() {
        ArrayList<Deportista> lista = new ArrayList<>();
        String selection = DeportistaContract.COLUMN_ACTIVO + " = 1";
        try {
            SQLiteDatabase database = managerDataBase.getReadableDatabase();
            try (Cursor cursor = database.query(DeportistaContract.TABLE_NAME,
                    null, selection, null, null, null,
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

    // Metodo para buscar un deportista por su documento (activo o no: el
    // documento es UNIQUE de verdad, asi que como mucho hay una fila)
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

    // Metodo para buscar un deportista por su id
    public Deportista obtenerPorId(String id) {
        String selection = DeportistaContract.COLUMN_ID + " = ?";
        String[] selectionArgs = {id};

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

    public enum ResultadoGuardado {
        CREADO, REACTIVADO, DOCUMENTO_DUPLICADO
    }

    // Si el documento pertenece a un deportista desactivado, reactiva esa
    // misma fila con los datos nuevos en vez de insertar una fila nueva; el
    // UNIQUE de dep_documento no lo permitiria de todas formas.
    public ResultadoGuardado crearOReactivar(Deportista deportista) {
        Deportista existente = obtenerPorDocumento(deportista.getDocumento());

        if (existente == null) {
            insertDeportista(deportista);
            return ResultadoGuardado.CREADO;
        }
        if (existente.isActivo()) {
            return ResultadoGuardado.DOCUMENTO_DUPLICADO;
        }

        deportista.setId(existente.getId());
        deportista.setActivo(true);
        actualizar(deportista);
        return ResultadoGuardado.REACTIVADO;
    }

    // Metodo para actualizar los datos de un deportista existente
    public int actualizar(Deportista deportista) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_NOMBRE, deportista.getNombre());
        values.put(DeportistaContract.COLUMN_DOCUMENTO, deportista.getDocumento());
        values.put(DeportistaContract.COLUMN_EDAD, deportista.getEdad());
        values.put(DeportistaContract.COLUMN_DISCIPLINA, deportista.getDisciplina());
        values.put(DeportistaContract.COLUMN_FOTO_PATH, deportista.getFotoPath());
        values.put(DeportistaContract.COLUMN_ACTIVO, deportista.isActivo() ? 1 : 0);
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {deportista.getId()};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            int filas = database.update(DeportistaContract.TABLE_NAME, values, whereClause, whereArgs);
            SyncScheduler.solicitarSincronizacion(context);
            return filas;
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar el deportista", e);
        }
        return -1;
    }

    // Soft-delete por auditoria: desactiva el deportista en vez de borrar la
    // fila. Queda excluido de obtenerTodos() pero conserva su historial.
    public int desactivar(String id) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_ACTIVO, 0);
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.PENDING);

        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            int filas = database.update(DeportistaContract.TABLE_NAME, values, whereClause, whereArgs);
            SyncScheduler.solicitarSincronizacion(context);
            return filas;
        } catch (Exception e) {
            Log.e(TAG, "Error al desactivar el deportista", e);
        }
        return -1;
    }

    // --- Sincronizacion de bajada: estos metodos escriben sync_status=SYNCED
    // directo (nunca PENDING) porque el cambio ya viene confirmado del
    // servidor. Si dejaran PENDING, el proximo push lo volveria a mandar sin
    // necesidad (y en el caso de eliminarLocalSolo, ni siquiera aplica: la
    // fila deja de existir). Tampoco disparan SyncScheduler: no hay nada que
    // subir, el origen del cambio es la API, no esta escritura local. ---

    public void insertDesdeServidor(Deportista deportista) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_ID, deportista.getId());
        values.put(DeportistaContract.COLUMN_NOMBRE, deportista.getNombre());
        values.put(DeportistaContract.COLUMN_DOCUMENTO, deportista.getDocumento());
        values.put(DeportistaContract.COLUMN_EDAD, deportista.getEdad());
        values.put(DeportistaContract.COLUMN_DISCIPLINA, deportista.getDisciplina());
        values.put(DeportistaContract.COLUMN_FOTO_PATH, deportista.getFotoPath());
        values.put(DeportistaContract.COLUMN_ACTIVO, deportista.isActivo() ? 1 : 0);
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.insert(DeportistaContract.TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar el deportista bajado del servidor", e);
        }
    }

    public void actualizarDesdeServidor(Deportista deportista) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_NOMBRE, deportista.getNombre());
        values.put(DeportistaContract.COLUMN_DOCUMENTO, deportista.getDocumento());
        values.put(DeportistaContract.COLUMN_EDAD, deportista.getEdad());
        values.put(DeportistaContract.COLUMN_DISCIPLINA, deportista.getDisciplina());
        values.put(DeportistaContract.COLUMN_FOTO_PATH, deportista.getFotoPath());
        values.put(DeportistaContract.COLUMN_ACTIVO, deportista.isActivo() ? 1 : 0);
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {deportista.getId()};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.update(DeportistaContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar el deportista bajado del servidor", e);
        }
    }

    public void desactivarDesdeServidor(String id) {
        ContentValues values = new ContentValues();
        values.put(DeportistaContract.COLUMN_ACTIVO, 0);
        values.put(DeportistaContract.COLUMN_SYNC_STATUS, SyncStatus.SYNCED);

        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.update(DeportistaContract.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al desactivar (bajada del servidor) el deportista", e);
        }
    }

    // Borrado fisico local: solo lo usa la reconciliacion de bajada cuando
    // confirma con la API (404) que la fila ya no existe del todo en el
    // servidor (ej. se borro directo en la base de datos).
    public void eliminarLocalSolo(String id) {
        String whereClause = DeportistaContract.COLUMN_ID + " = ?";
        String[] whereArgs = {id};

        try {
            SQLiteDatabase database = managerDataBase.getWritableDatabase();
            database.delete(DeportistaContract.TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar localmente el deportista (bajada del servidor)", e);
        }
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
        deportista.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_ACTIVO)) == 1);
        deportista.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(DeportistaContract.COLUMN_SYNC_STATUS)));
        return deportista;
    }
}
