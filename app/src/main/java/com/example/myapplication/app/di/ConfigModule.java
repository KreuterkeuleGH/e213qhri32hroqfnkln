package com.example.myapplication.app.di;

import javax.inject.Named;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

@Module
public class ConfigModule {

    @Provides @Singleton
    @Named("batch_size")
    int provideBatchSize() {
        return 50;
    }

    // ggf. weitere Konfigurationen …
}
