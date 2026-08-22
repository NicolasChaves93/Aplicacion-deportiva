package com.entrenaapp.mobile.data.local.managerdb;

public final class AsistenciaContract {
    private AsistenciaContract() {
        // impide crear objetos de esta clase
    }

    public static final String TABLE_NAME = "asistencias";
    public static final String COLUMN_ID = "asi_id";
    public static final String COLUMN_ENTRENAMIENTO_ID = "asi_entrenamiento_id";
    public static final String COLUMN_DEPORTISTA_ID = "asi_deportista_id";
    public static final String COLUMN_ASISTIO = "asi_asistio";
    public static final String COLUMN_OBSERVACION = "asi_observacion";
    public static final String COLUMN_SYNC_STATUS = "asi_sync_status";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_ENTRENAMIENTO_ID + " TEXT NOT NULL, " +
                    COLUMN_DEPORTISTA_ID + " TEXT NOT NULL, " +
                    COLUMN_ASISTIO + " INTEGER NOT NULL, " +
                    COLUMN_OBSERVACION + " TEXT, " +
                    COLUMN_SYNC_STATUS + " TEXT NOT NULL DEFAULT '" + SyncStatus.PENDING + "' " +
                    "CHECK (" + COLUMN_SYNC_STATUS + " IN ('" + SyncStatus.PENDING + "', '" + SyncStatus.SYNCED + "')), " +
                    "FOREIGN KEY (" + COLUMN_ENTRENAMIENTO_ID + ") REFERENCES " +
                    EntrenamientoContract.TABLE_NAME + "(" + EntrenamientoContract.COLUMN_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + COLUMN_DEPORTISTA_ID + ") REFERENCES " +
                    DeportistaContract.TABLE_NAME + "(" + DeportistaContract.COLUMN_ID + ") ON DELETE CASCADE)";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
