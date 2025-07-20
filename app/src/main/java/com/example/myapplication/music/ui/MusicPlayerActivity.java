package com.example.myapplication.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.music.worker.CleanupWorker;
import com.example.myapplication.music.domain.MusicFolderHelper;
import com.example.myapplication.R;
import com.example.myapplication.MusicSettingsActivity;
import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;
import com.example.myapplication.music.worker.MusicLoaderWorker;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;


import Controller.MusicPlayerController;

public class MusicPlayerActivity extends AppCompatActivity implements MusicPlayerController.MusicPlayerListener {
    private static final String TAG = "MusicPlayerActivity";



    private MusicPlayerController playerController;
    private SeekBar seekBar;
    private TextView lblTrackTitle;
    private Button btnPlayPause;
    private Button btnAllTracks, btnPlaylists, btnNewTitles, btnPlaybackList, btnEqualizer;
    private Button btnSettings;

    private ArrayList<Track> trackList = new ArrayList<>();
    private int currentTrackIndex = -1;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 50;
    private boolean isTrackLoaded = false;
    private String folderUriStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        scheduleCleanupWorker();
        schedulePeriodicMusicScan();

        // Hole den Musikordner-URI
        initializeFolderUri();
        // Initialisiert alle benötigten UI-Elemente und setzt entsprechende Listener
        initializeUI();

        // Initialisiere den MediaPlayer-Controller und registriere das Listener-Interface
        playerController = new MusicPlayerController(this);

        // Starte das Laden der Trackliste, sofern ein Musikordner festgelegt ist
        if (folderUriStr != null && !folderUriStr.isEmpty()) {
            loadInitialTracks();
        } else {
            Toast.makeText(this,
                    "Kein Musikordner festgelegt! Bitte wähle in den Einstellungen einen Musikordner aus.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void schedulePeriodicMusicScan() {
        PeriodicWorkRequest scanRequest =
                new PeriodicWorkRequest.Builder(MusicLoaderWorker.class, 15, TimeUnit.MINUTES)
                        // Optional: setAdditionalConstraints() hinzufügen, falls benötigt.
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("PeriodicMusicScan",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        scanRequest);
    }

    private void scheduleCleanupWorker() {
        // Starte einen PeriodicWorkRequest, z.B. alle 24 Stunden.
        PeriodicWorkRequest cleanupRequest =
                new PeriodicWorkRequest.Builder(CleanupWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("CleanupWorker",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        cleanupRequest);
    }

    /**
     * Ermittelt den Musikordner-URI mithilfe des MusicFolderHelper.
     */
    private void initializeFolderUri() {
        folderUriStr = MusicFolderHelper.getMusicFolderUri(this);
    }

    /**
     * Initialisiert alle UI-Elemente und setzt Click-Listener.
     */
    private void initializeUI() {
        lblTrackTitle = findViewById(R.id.lblTrackTitle);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnSettings = findViewById(R.id.button_musik_einstellungen);
        btnAllTracks = findViewById(R.id.btnAllTracks);
        btnPlaylists = findViewById(R.id.btnPlaylists);
        btnNewTitles = findViewById(R.id.btnNewTitles);
        btnPlaybackList = findViewById(R.id.btnPlaybackList);
        btnEqualizer = findViewById(R.id.btnEqualizer);

        btnSettings.setOnClickListener(view -> {
            Intent openSettings = new Intent(MusicPlayerActivity.this, MusicSettingsActivity.class);
            startActivity(openSettings);
        });

        btnAllTracks.setOnClickListener(v -> {
            Intent intent = new Intent(MusicPlayerActivity.this, AllTracksActivity.class);
            startActivity(intent);
        });
        btnPlaylists.setOnClickListener(v -> Toast.makeText(MusicPlayerActivity.this,
                "Playlists werden angezeigt", Toast.LENGTH_SHORT).show());
        btnNewTitles.setOnClickListener(v -> Log.i(TAG, "Neue Titel-Button geklickt"));

        btnPlayPause.setOnClickListener(v -> {
            if (!isTrackLoaded) {
                if (!trackList.isEmpty()) {
                    currentTrackIndex = 0;
                    playCurrentTrack();
                    isTrackLoaded = true;
                } else {
                    Toast.makeText(MusicPlayerActivity.this, "Keine Titel gefunden!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Toggle Play/Pause – Pause oder Resume über den Controller
                if (btnPlayPause.getText().toString().equals("⏸")) {
                    playerController.pause();
                    btnPlayPause.setText("▶");
                } else {
                    playerController.resume();
                    btnPlayPause.setText("⏸");
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Für das direkte Springen im Track könnte man eine Methode im Controller ergänzen
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Lädt asynchron die initiale Trackliste.
     */
    private void loadInitialTracks() {
        if (folderUriStr == null || folderUriStr.isEmpty()) {
            return;
        }
        currentPage = 0;
        new Thread(() -> {
            MusicRepository repository = MusicRepository.getInstance(getApplicationContext());
            ArrayList<Track> loadedTracks = new ArrayList<>(repository.getCachedTracksPage(currentPage, PAGE_SIZE, folderUriStr));

            runOnUiThread(() -> {
                trackList = loadedTracks;
                if (!trackList.isEmpty()) {
                    currentPage++;
                } else {
                    Toast.makeText(MusicPlayerActivity.this,
                            "Keine Titel gefunden. Bitte füge einen Musikordner hinzu.",
                            Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "Loaded tracks: " + trackList.size());
            });
        }).start();
    }

    /**
     * Lädt asynchron die nächste Seite von Tracks und spielt sie gegebenenfalls ab.
     */
    private void loadNextPage(final boolean playAfterLoad) {
        if (folderUriStr == null || folderUriStr.isEmpty()) {
            Toast.makeText(MusicPlayerActivity.this, "Kein Musikordner ausgewählt!", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            MusicRepository repository = MusicRepository.getInstance(getApplicationContext());
            List<Track> newTracks = repository.getCachedTracksPage(currentPage, PAGE_SIZE, folderUriStr);
            runOnUiThread(() -> {
                if (newTracks != null && !newTracks.isEmpty()) {
                    trackList.addAll(newTracks);
                    currentPage++;
                    if (playAfterLoad) {
                        currentTrackIndex++;
                        if (currentTrackIndex < trackList.size()) {
                            playCurrentTrack();
                        }
                    }
                } else {
                    Toast.makeText(MusicPlayerActivity.this, "Keine weiteren Titel vorhanden", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Delegiert die Wiedergabe des aktuellen Tracks an den MusicPlayerController.
     */
    private void playCurrentTrack() {
        if (trackList == null || currentTrackIndex < 0 || currentTrackIndex >= trackList.size()) {
            return;
        }
        Track currentTrack = trackList.get(currentTrackIndex);
        playerController.playTrack(this, currentTrack);
    }

    private void playNextTrack() {
        if (trackList != null && currentTrackIndex < trackList.size() - 1) {
            currentTrackIndex++;
            playCurrentTrack();
        } else {
            loadNextPage(true);
        }
    }

    private void playPreviousTrack() {
        if (trackList != null && currentTrackIndex > 0) {
            currentTrackIndex--;
            playCurrentTrack();
        } else {
            Toast.makeText(this, "Kein vorheriger Titel vorhanden", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerController != null) {
            playerController.release();
        }
    }

    // --- Implementierung der MusicPlayerListener-Methoden ---
    @Override
    public void onTrackStarted(String title, int duration) {
        btnPlayPause.setText("⏸");
        lblTrackTitle.setText(title);
        seekBar.setMax(duration);
    }

    @Override
    public void onProgressUpdated(int progress) {
        seekBar.setProgress(progress);
    }

    @Override
    public void onTrackCompleted() {
        playNextTrack();
    }
}
