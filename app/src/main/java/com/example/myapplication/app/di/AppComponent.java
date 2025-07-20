package com.example.myapplication.app.di;

import com.example.myapplication.app.MainActivity;
import com.example.myapplication.app.MyApplication;
import com.example.myapplication.music.ui.AllTracksActivity;
import com.example.myapplication.music.worker.MusicLoaderWorker;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
        MultimediaModule.class,
        ConfigModule.class
})
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(AllTracksActivity activity);
    void inject(MusicLoaderWorker worker);

    void inject(MyApplication myApplication);
}
