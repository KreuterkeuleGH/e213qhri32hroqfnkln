package com.example.myapplication.app;

import android.app.Application;
import com.example.myapplication.app.di.AppComponent;
import com.example.myapplication.app.di.DaggerAppComponent;
import com.example.myapplication.app.di.ConfigModule;
import com.example.myapplication.app.di.MultimediaModule;
import javax.inject.Inject;

public class MultimediaApp extends Application {
    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .configModule(new ConfigModule())
                .multimediaModule(new MultimediaModule(this))
                .build();
    }

    public static AppComponent getComponent() {
        return component;
    }
}
