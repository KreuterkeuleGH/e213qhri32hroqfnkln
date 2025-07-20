package com.example.myapplication.app.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.music.data.MusicRepository;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class MultimediaModule {
    private final Application app;

    public MultimediaModule(Application app) {
        this.app = app;
    }

    @Provides @Singleton
    Application provideApplication() {
        return app;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return app.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE);
    }

    // <<< Neu: So kann Dagger MusicRepository injizieren >>>
    @Provides @Singleton
    MusicRepository provideMusicRepository(Application app) {
        return MusicRepository.getInstance(app);
    }
}
