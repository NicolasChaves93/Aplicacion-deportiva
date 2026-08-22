package com.entrenaapp.mobile;

import android.app.Application;

import com.entrenaapp.mobile.data.sync.SyncScheduler;

public class EntrenaAppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SyncScheduler.programarSincronizacionPeriodica(this);
    }
}
