package com.example.myapplication.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.app.MyApplication;          // <-- hier dein Application-Subclass
import com.example.myapplication.databinding.ActivityAllTracksBinding;
import com.example.myapplication.music.domain.FolderItem;
import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.domain.FolderManager;
import com.example.myapplication.music.ui.adapter.TrackAdapter;
import com.example.myapplication.music.ui.viewmodel.TrackViewModel;
import com.example.myapplication.music.worker.MusicScanScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class AllTracksActivity extends AppCompatActivity {
    private ActivityAllTracksBinding binding;
    private TrackViewModel trackViewModel;
    private TrackAdapter adapter;

    @Inject MusicScanScheduler scheduler;
    @Inject FolderManager    folderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Dagger-Graph injizieren über deine Application-Klasse
        MyApplication.getAppComponent().inject(this);
        // 2) ViewBinding initialisieren
        binding = ActivityAllTracksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3) Adapter mit Klick-Callback anlegen
        adapter = new TrackAdapter(track -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("track_id", track.getId());
            startActivity(intent);
        });

        // 4) RecyclerView einrichten
        binding.recyclerTracks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTracks.setAdapter(adapter);

        // 5) Debug-Datenbank-Log off-main-thread
        Executors.newSingleThreadExecutor().execute(() ->
                MusicRepository.getInstance(getApplicationContext()).debugDatabase()
        );

        // 6) ViewModel + LiveData
        trackViewModel = new ViewModelProvider(this).get(TrackViewModel.class);
        trackViewModel.getAllTracks().observe(this, tracks -> {
            if (tracks == null || tracks.isEmpty()) {
                Toast.makeText(this, "Keine Tracks gefunden", Toast.LENGTH_SHORT).show();
            }
            adapter.submitList(tracks);
        });

        // 7) Periodischen Scan planen
        scheduler.schedulePeriodic();

        // 8) Ordner-URIs aus Preferences holen
        Set<FolderItem> items = folderManager.getFolderItems();
        String foldersCsv = null;
        if (items != null && !items.isEmpty()) {
            List<String> uriList = new ArrayList<>(items.size());
            for (FolderItem item : items) {
                uriList.add(item.getUri());
            }
            foldersCsv = String.join(",", uriList);
        }

        if (foldersCsv == null) {
            Toast.makeText(this, "Keine Musikordner ausgewählt", Toast.LENGTH_SHORT).show();
            return;
        }

        // 9) Initialer OneTime-Scan
        scheduler.triggerOneTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ohne Ordner-Parameter neue Dateien erfassen
        scheduler.triggerOneTime();
    }
}
