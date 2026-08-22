package com.entrenaapp.mobile.managerdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ManagerDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "entrenaapp.db";
    private static final int DATABASE_VERSION = 1;

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
        database.execSQL(AsistenciaContract.DROP_TABLE);
        database.execSQL(EntrenamientoContract.DROP_TABLE);
        database.execSQL(DeportistaContract.DROP_TABLE);
        onCreate(database);
    }
}
