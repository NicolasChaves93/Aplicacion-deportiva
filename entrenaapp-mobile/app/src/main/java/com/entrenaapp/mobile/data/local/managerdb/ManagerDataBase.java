package com.entrenaapp.mobile.data.local.managerdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ManagerDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "entrenaapp.db";
    // v2: agrega dep_activo (soft-delete). v3: restaura el UNIQUE real de
    // dep_documento (crear con el documento de un inactivo lo reactiva).
    // v4: repara la FOREIGN KEY de asistencias, que quedo apuntando a la
    // tabla temporal "deportistas_old" tras los RENAME de las migraciones
    // v2/v3 (SQLite reescribe el texto de la FK en las tablas hijas cuando se
    // hace ALTER TABLE ... RENAME, y al encadenar dos renames se corrompio).
    private static final int DATABASE_VERSION = 4;

    public ManagerDataBase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DeportistaContract.CREATE_TABLE);
        database.execSQL(EntrenamientoContract.CREATE_TABLE);
        database.execSQL(AsistenciaContract.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrarDeportistasV1AV2(database);
        }
        if (oldVersion < 3) {
            migrarDeportistasV2AV3(database);
        }
        if (oldVersion < 4) {
            repararForeignKeyAsistencias(database);
        }
    }

    // Reconstruye la tabla deportistas para agregar dep_activo y quitar el
    // UNIQUE de dep_documento, conservando los datos ya guardados en el
    // dispositivo (a diferencia de un DROP+CREATE, que los perderia).
    private void migrarDeportistasV1AV2(SQLiteDatabase database) {
        String tablaTemporal = DeportistaContract.TABLE_NAME + "_old";
        database.execSQL("ALTER TABLE " + DeportistaContract.TABLE_NAME + " RENAME TO " + tablaTemporal);
        database.execSQL(DeportistaContract.CREATE_TABLE);
        database.execSQL(
                "INSERT INTO " + DeportistaContract.TABLE_NAME + " (" +
                        DeportistaContract.COLUMN_ID + ", " +
                        DeportistaContract.COLUMN_NOMBRE + ", " +
                        DeportistaContract.COLUMN_DOCUMENTO + ", " +
                        DeportistaContract.COLUMN_EDAD + ", " +
                        DeportistaContract.COLUMN_DISCIPLINA + ", " +
                        DeportistaContract.COLUMN_FOTO_PATH + ", " +
                        DeportistaContract.COLUMN_ACTIVO + ", " +
                        DeportistaContract.COLUMN_SYNC_STATUS + ") " +
                        "SELECT " +
                        DeportistaContract.COLUMN_ID + ", " +
                        DeportistaContract.COLUMN_NOMBRE + ", " +
                        DeportistaContract.COLUMN_DOCUMENTO + ", " +
                        DeportistaContract.COLUMN_EDAD + ", " +
                        DeportistaContract.COLUMN_DISCIPLINA + ", " +
                        DeportistaContract.COLUMN_FOTO_PATH + ", 1, " +
                        DeportistaContract.COLUMN_SYNC_STATUS +
                        " FROM " + tablaTemporal);
        database.execSQL("DROP TABLE " + tablaTemporal);
    }

    // Restaura el UNIQUE de dep_documento. Si por pruebas locales llegaron a
    // quedar dos filas con el mismo documento (algo que ya no puede pasar en
    // la app, pero pudo pasar mientras la columna no tenia UNIQUE), se
    // conserva solo una por documento: la activa, o si ninguna lo esta, la
    // mas reciente.
    private void migrarDeportistasV2AV3(SQLiteDatabase database) {
        String tablaTemporal = DeportistaContract.TABLE_NAME + "_old";
        database.execSQL("ALTER TABLE " + DeportistaContract.TABLE_NAME + " RENAME TO " + tablaTemporal);
        database.execSQL(DeportistaContract.CREATE_TABLE);
        database.execSQL(
                "INSERT INTO " + DeportistaContract.TABLE_NAME + " SELECT o.* FROM " + tablaTemporal + " o " +
                        "WHERE o." + DeportistaContract.COLUMN_ID + " = (" +
                        "SELECT o2." + DeportistaContract.COLUMN_ID + " FROM " + tablaTemporal + " o2 " +
                        "WHERE o2." + DeportistaContract.COLUMN_DOCUMENTO + " = o." + DeportistaContract.COLUMN_DOCUMENTO + " " +
                        "ORDER BY o2." + DeportistaContract.COLUMN_ACTIVO + " DESC, o2.rowid DESC LIMIT 1)");
        database.execSQL("DROP TABLE " + tablaTemporal);
    }

    // Reconstruye asistencias con AsistenciaContract.CREATE_TABLE (que
    // referencia "deportistas"/"entrenamientos" por su nombre actual),
    // reemplazando la FOREIGN KEY corrupta que quedo apuntando a la tabla
    // temporal ya eliminada "deportistas_old".
    private void repararForeignKeyAsistencias(SQLiteDatabase database) {
        String tablaTemporal = AsistenciaContract.TABLE_NAME + "_old";
        database.execSQL("ALTER TABLE " + AsistenciaContract.TABLE_NAME + " RENAME TO " + tablaTemporal);
        database.execSQL(AsistenciaContract.CREATE_TABLE);
        database.execSQL(
                "INSERT INTO " + AsistenciaContract.TABLE_NAME + " (" +
                        AsistenciaContract.COLUMN_ID + ", " +
                        AsistenciaContract.COLUMN_ENTRENAMIENTO_ID + ", " +
                        AsistenciaContract.COLUMN_DEPORTISTA_ID + ", " +
                        AsistenciaContract.COLUMN_ASISTIO + ", " +
                        AsistenciaContract.COLUMN_OBSERVACION + ", " +
                        AsistenciaContract.COLUMN_SYNC_STATUS + ") " +
                        "SELECT " +
                        AsistenciaContract.COLUMN_ID + ", " +
                        AsistenciaContract.COLUMN_ENTRENAMIENTO_ID + ", " +
                        AsistenciaContract.COLUMN_DEPORTISTA_ID + ", " +
                        AsistenciaContract.COLUMN_ASISTIO + ", " +
                        AsistenciaContract.COLUMN_OBSERVACION + ", " +
                        AsistenciaContract.COLUMN_SYNC_STATUS +
                        " FROM " + tablaTemporal);
        database.execSQL("DROP TABLE " + tablaTemporal);
    }
}
