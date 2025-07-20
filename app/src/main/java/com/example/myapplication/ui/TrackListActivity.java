package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.ui.adapter.TrackListAdapter;
import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackListActivity extends AppCompatActivity {

    private ListView listViewTracks;
    private TrackListAdapter adapter;
    private final List<Track> trackList = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 50;
    private boolean isLoading = false;
    // Beispielhafter Ordner-URI. Normalerweise wird dieser per Intent übergeben.
    private final String folderUriStr = "content://com.example.provider/your_folder_uri";
    private MusicRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        // Initialisiere das Repository (Model)
        repository = MusicRepository.getInstance(getApplicationContext());
        listViewTracks = findViewById(R.id.listViewTracks);
        adapter = new TrackListAdapter(this, trackList);
        listViewTracks.setAdapter(adapter);

        // Lade die erste Seite von Tracks
        loadNextPage();

        // Setze einen Scroll-Listener, um weitere Seiten zu laden, sobald der Benutzer nahe dem Ende der Liste scrollt.
        listViewTracks.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Hier ist keine weitere Aktion erforderlich
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Wenn weniger als 5 Einträge nach dem aktuell sichtbaren Bereich vorhanden sind, lade die nächste Seite.
                if (!isLoading && (totalItemCount - (firstVisibleItem + visibleItemCount)) <= 5) {
                    loadNextPage();
                }
            }
        });
    }

    /**
     * Lädt asynchron die nächste Seite von Tracks aus der Datenbank und fügt diese der ListView hinzu.
     * Bei Erfolg wird die neue Seite der bestehenden Liste hinzugefügt und der aktuelle Seitenzähler erhöht.
     * Wird keine weitere Seite gefunden, erfolgt eine kurze Rückmeldung an den Nutzer.
     */
    private void loadNextPage() {
        isLoading = true;
        new Thread(() -> {
            final List<Track> newTracks = repository.getCachedTracksPage(currentPage, PAGE_SIZE, "");
            runOnUiThread(() -> {
                if (newTracks != null && !newTracks.isEmpty()) {
                    trackList.addAll(newTracks);
                    adapter.notifyDataSetChanged();
                    currentPage++;
                } else {
                    Toast.makeText(TrackListActivity.this, "Keine weiteren Titel gefunden.", Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
            });
        }).start();
    }
}
