package com.example.myapplication.music.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;

import java.util.List;

/**
 * TrackViewModel stellt die Track-Daten als LiveData bereit und kapselt so den Zugriff auf das MusicRepository.
 * Dadurch wird die UI automatisch benachrichtigt, wenn sich die Daten ändern.
 */
public class TrackViewModel extends AndroidViewModel {
    private MusicRepository repository;
    private LiveData<List<Track>> allTracks;

    public TrackViewModel(@NonNull Application application) {
        super(application);
        repository = MusicRepository.getInstance(application);
        allTracks = repository.getAllTracksLiveData();
    }

    public LiveData<List<Track>> getAllTracks() {
        return allTracks;
    }

    // Optional: Falls du die Daten aktualisieren möchtest, kannst du auch eine Methode hinzufügen:
    public void refreshTracks() {
        allTracks = repository.getAllTracksLiveData();
    }
}
