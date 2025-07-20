package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * TitleListActivity zeigt eine Liste der Track-Titel an, die aus der Datenbank (Model) geladen werden.
 * Die Activity ist dabei für die Darstellung (View) zuständig.
 */
public class TitleListActivity extends AppCompatActivity {

    private ListView listViewTitles;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> titleDisplayList = new ArrayList<>();
    private MusicRepository repository;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_list);

        // UI-Komponenten initialisieren
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        listViewTitles = findViewById(R.id.listViewTitles);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titleDisplayList);
        listViewTitles.setAdapter(adapter);

        // Repository instanziieren (Zugriff auf das Model)
        repository = MusicRepository.getInstance(this);

        // Lade initial die Trackliste
        loadTrackTitles();

        // Ermögliche manuelles Aktualisieren per Pull-to-Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> loadTrackTitles());
    }

    /**
     * Lädt asynchron die Trackliste aus der Datenbank und aktualisiert die ListView.
     * Dabei wird im UI-Thread verhindert, dass die Anzeige blockiert wird.
     */
    private void loadTrackTitles() {
        new Thread(() -> {
            // Lese alle in der DB gespeicherten Tracks (Model)
            List<Track> tracks = repository.getCachedTracks();
            runOnUiThread(() -> {
                titleDisplayList.clear();
                if (tracks != null && !tracks.isEmpty()) {
                    for (Track track : tracks) {
                        titleDisplayList.add(track.getTitle());
                    }
                } else {
                    Toast.makeText(TitleListActivity.this, "Keine Titel gefunden", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(TitleListActivity.this, "Aktualisiert", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
