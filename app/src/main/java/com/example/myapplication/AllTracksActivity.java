package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.ActivityAllTracksBinding;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AllTracksActivity extends AppCompatActivity {
    private ActivityAllTracksBinding binding;
    private TrackViewModel trackViewModel;
    private TrackAdapter adapter = new TrackAdapter();

    @Inject MusicScanScheduler scheduler;
    @Inject FolderManager folderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllTracksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // RecyclerView einrichten
        binding.recyclerTracks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTracks.setAdapter(adapter);

        // Debugging der DB off-main-thread
        Executors.newSingleThreadExecutor().execute(() ->
                MusicRepository.getInstance(getApplicationContext()).debugDatabase()
        );

        // LiveData-Beobachtung: UI reagiert automatisch auf DB-Updates
        trackViewModel = new ViewModelProvider(this).get(TrackViewModel.class);
        trackViewModel.getAllTracks().observe(this, tracks -> {
            if (tracks == null || tracks.isEmpty()) {
                Toast.makeText(this, "Keine Tracks gefunden", Toast.LENGTH_SHORT).show();
            }
            adapter.submitList(tracks);
        });

        // Periodischen Scan einmalig planen
        scheduler.schedulePeriodic();

        // Ordner-URIs aus Preferences holen und CSV bauen
        Set<FolderItem> items = folderManager.getFolderItems();
        String foldersCsv = (items == null || items.isEmpty())
                ? null
                : items.stream()
                .map(FolderItem::getUri)
                .collect(Collectors.joining(","));

        if (foldersCsv == null) {
            Toast.makeText(this,
                    "Keine Musikordner ausgewählt", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialer OneTime-Scan mit definierten Ordnern
        scheduler.triggerOneTime(foldersCsv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // OneTime-Scan ohne Ordner-Parameter, um neue Dateien zu erfassen
        scheduler.triggerOneTime(null);
    }
}
