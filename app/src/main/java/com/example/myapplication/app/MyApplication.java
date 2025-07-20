package com.example.myapplication.app;

import android.app.Application;

import androidx.work.WorkManager;

import com.example.myapplication.app.di.AppComponent;
import com.example.myapplication.app.di.DaggerAppComponent;
import com.example.myapplication.app.di.ConfigModule;
import com.example.myapplication.app.di.MultimediaModule;

public class MyApplication extends Application {

    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1) Alte WorkManager-Jobs abbrechen (falls noch Reste aus Development existieren)
        WorkManager.getInstance(this).cancelAllWork();

        // 2) Dagger-Graph einmalig bauen
        appComponent = DaggerAppComponent.builder()
                .configModule(new ConfigModule())
                .multimediaModule(new MultimediaModule(this))
                .build();
    }

    /**
     * Liefert den Dagger-Component, um in Activities/Workern zu injecten.
     */
    public static AppComponent getAppComponent() {
        return appComponent;
    }
}
