package com.example.myapplication.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;

import java.io.IOException;
import java.util.List;

public class MusicPlayerService extends Service {
    private static final String TAG = "MusicPlayerService";

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private List<Track> cachedTracks;
    private MusicRepository repository;

    // Observer, der auf Änderungen des Track-LiveData reagiert
    private final Observer<List<Track>> tracksObserver = new Observer<List<Track>>() {
        @Override
        public void onChanged(List<Track> tracks) {
            cachedTracks = tracks; // Aktualisiere hier deine lokale Trackliste
            Log.d(TAG, "Trackliste aktualisiert im Service: " + (tracks != null ? tracks.size() : 0) + " Titel");
        }
    };

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> playNextTrack());

        // Initialisiere das Repository und beobachte die Trackliste per LiveData.
        repository = MusicRepository.getInstance(getApplicationContext());
        // observeForever verwendet keinen LifecycleOwner – vergesst nicht, den Observer in onDestroy() zu entfernen.
        repository.getAllTracksLiveData().observeForever(tracksObserver);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Spielt den Track an der angegebenen Position.
     */
    public void playTrack(int index) {
        if (cachedTracks == null || cachedTracks.isEmpty() || index < 0 || index >= cachedTracks.size()) {
            Log.w(TAG, "Kein gültiger Track gefunden oder Index ungültig.");
            return;
        }
        Track track = cachedTracks.get(index);
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            Uri trackUri = Uri.parse(track.getUri());
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.i(TAG, "Started playing: " + track.getTitle());
        } catch (IOException e) {
            Log.e(TAG, "Error playing track: " + track.getTitle(), e);
        }
    }

    /**
     * Spielt den nächsten Track.
     */
    public void playNextTrack() {
        if (cachedTracks != null && !cachedTracks.isEmpty()) {
            // Dies könnte z. B. den nächsten Index in der Liste nutzen.
            playTrack(0); // Beispiel: immer den ersten Track abspielen
        }
    }

    @Override
    public void onDestroy() {
        // Wichtig: Entferne den Forever-Observer, um Memory Leaks zu vermeiden.
        repository.getAllTracksLiveData().removeObserver(tracksObserver);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
