package com.entrenaapp.mobile.managerdb;

public final class EntrenamientoContract {
    private EntrenamientoContract() {
        // impide crear objetos de esta clase
    }

    public static final String TABLE_NAME = "entrenamientos";
    public static final String COLUMN_ID = "ent_id";
    public static final String COLUMN_FECHA = "ent_fecha";
    public static final String COLUMN_TIPO = "ent_tipo";
    public static final String COLUMN_DURACION_MIN = "ent_duracion_min";
    public static final String COLUMN_INTENSIDAD = "ent_intensidad";
    public static final String COLUMN_LATITUD = "ent_latitud";
    public static final String COLUMN_LONGITUD = "ent_longitud";
    public static final String COLUMN_AUDIO_PATH = "ent_audio_path";
    public static final String COLUMN_SYNC_STATUS = "ent_sync_status";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_FECHA + " TEXT NOT NULL, " +
                    COLUMN_TIPO + " TEXT NOT NULL, " +
                    COLUMN_DURACION_MIN + " INTEGER NOT NULL, " +
                    COLUMN_INTENSIDAD + " TEXT NOT NULL, " +
                    COLUMN_LATITUD + " REAL, " +
                    COLUMN_LONGITUD + " REAL, " +
                    COLUMN_AUDIO_PATH + " TEXT, " +
                    COLUMN_SYNC_STATUS + " TEXT NOT NULL DEFAULT '" + SyncStatus.PENDING + "' " +
                    "CHECK (" + COLUMN_SYNC_STATUS + " IN ('" + SyncStatus.PENDING + "', '" + SyncStatus.SYNCED + "')))";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
