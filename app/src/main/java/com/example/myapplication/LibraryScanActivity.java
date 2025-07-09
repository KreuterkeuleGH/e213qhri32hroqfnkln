package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Controller.LibraryScanController;

public class LibraryScanActivity extends AppCompatActivity {

    private List<String> trackList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listViewTracks;
    private LibraryScanController libraryScanController;
    private String selectedFolderUri; // Wird via Intent extra übergeben

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_scan);

        // Hole den übergebenen Folder-URI (nur den Inhalt dieses Ordners anzeigen)
        selectedFolderUri = getIntent().getStringExtra("folder_uri");
        if (selectedFolderUri == null || selectedFolderUri.trim().isEmpty()) {
            Toast.makeText(this, "Kein Ordner ausgewählt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Starte den periodischen Scan für den ausgewählten Ordner
        schedulePeriodicMusicScan();

        initViews();

        // Initialisiere den LibraryScanController und starte den Scan für den ausgewählten Ordner.
        // Die Methode scanLibrary(LifecycleOwner, String folderUri, LibraryScanCallback callback)
        // liefert über den Callback die Liste der gefundenen Tracktitel.
        libraryScanController = new LibraryScanController(getApplicationContext());
        libraryScanController.scanLibrary(this, selectedFolderUri, (List<String> tracks) -> {
            updateUIWithTracks(tracks);
        });

        // Zusätzlich: Starte einen OneTimeWorkRequest, der den MusicLoaderWorker mit dem
        // ausgewählten Folder-URI aufruft und intern in Batches arbeitet.
        Data inputData = new Data.Builder()
                .putString("folder_uris", selectedFolderUri) // Nur den ausgewählten Ordner-URI übergeben
                .putInt("page_size", 50)
                .build();

        OneTimeWorkRequest musicLoaderRequest =
                new OneTimeWorkRequest.Builder(MusicLoaderWorker.class)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork("MusicLoaderSingleFolder", ExistingWorkPolicy.REPLACE, musicLoaderRequest);
    }

    private void initViews() {
        listViewTracks = findViewById(R.id.listViewTracks);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trackList);
        listViewTracks.setAdapter(adapter);
    }

    private void updateUIWithTracks(List<String> loadedTracks) {
        trackList.clear();
        trackList.addAll(loadedTracks);
        adapter.notifyDataSetChanged();
    }

    /**
     * Plant einen periodischen Scan, der alle 15 Minuten den Inhalt des ausgewählten Ordners einliest.
     */
    private void schedulePeriodicMusicScan() {
        PeriodicWorkRequest scanRequest =
                new PeriodicWorkRequest.Builder(MusicLoaderWorker.class, 15, TimeUnit.MINUTES)
                        .setInputData(
                                new Data.Builder()
                                        .putString("folder_uris", selectedFolderUri)
                                        .putInt("page_size", 50)
                                        .build()
                        )
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("PeriodicMusicScanSingleFolder",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        scanRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Beim erneuten Aufrufen der Activity wird ein OneTime-Scan getriggert,
        // damit der aktuelle Inhalt des ausgewählten Ordners aktualisiert wird.
        triggerMusicScan();
    }

    /**
     * Startet einen OneTimeWorkRequest, um den aktuellen Ordner-Inhalt sofort neu einzulesen.
     */
    private void triggerMusicScan() {
        OneTimeWorkRequest scanRequest = new OneTimeWorkRequest.Builder(MusicLoaderWorker.class)
                .setInputData(
                        new Data.Builder()
                                .putString("folder_uris", selectedFolderUri)
                                .putInt("page_size", 50)
                                .build()
                )
                .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork("MusicLoaderOnLibraryScan", ExistingWorkPolicy.REPLACE, scanRequest);
    }
}
