package com.entrenaapp.mobile.data.local.managerdb;

public final class DeportistaContract {
    private DeportistaContract() {
        // impide crear objetos de esta clase
    }

    public static final String TABLE_NAME = "deportistas";
    public static final String COLUMN_ID = "dep_id";
    public static final String COLUMN_NOMBRE = "dep_nombre";
    public static final String COLUMN_DOCUMENTO = "dep_documento";
    public static final String COLUMN_EDAD = "dep_edad";
    public static final String COLUMN_DISCIPLINA = "dep_disciplina";
    public static final String COLUMN_FOTO_PATH = "dep_foto_path";
    public static final String COLUMN_SYNC_STATUS = "dep_sync_status";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_DOCUMENTO + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EDAD + " INTEGER NOT NULL, " +
                    COLUMN_DISCIPLINA + " TEXT NOT NULL, " +
                    COLUMN_FOTO_PATH + " TEXT, " +
                    COLUMN_SYNC_STATUS + " TEXT NOT NULL DEFAULT '" + SyncStatus.PENDING + "' " +
                    "CHECK (" + COLUMN_SYNC_STATUS + " IN ('" + SyncStatus.PENDING + "', '" + SyncStatus.SYNCED + "')))";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
