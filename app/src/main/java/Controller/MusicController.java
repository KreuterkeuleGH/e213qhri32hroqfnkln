package Controller;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import com.example.myapplication.MusicRepository;
import com.example.myapplication.Track;
import java.util.List;

public class MusicController extends AppCompatActivity {

    private MusicRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setze gegebenenfalls dein Layout, falls nötig
        // setContentView(R.layout.dein_layout);

        // Initialisiere das Repository
        repository = MusicRepository.getInstance(getApplicationContext());


        // Starte das Laden der Tracks
        loadTracks();
    }

    /**
     * Lädt alle Tracks aus der Datenbank asynchron über LiveData und verarbeitet sie.
     */
    public void loadTracks() {
        // "getAllTracks()" liefert LiveData<List<Track>> zurück.
        repository.getAllTracksLiveData().observe(this, new Observer<List<Track>>() {
            @Override
            public void onChanged(List<Track> tracks) {
                if (tracks != null && !tracks.isEmpty()) {
                    // Hier kannst du die erhaltenen Tracks weiterverarbeiten,
                    // zum Beispiel an einen Adapter übergeben oder in der UI anzeigen.
                    // Beispiel:
                    for (Track track : tracks) {
                        // Debug-Ausgabe oder UI-Update
                        System.out.println("Geladener Track: " + track.getTitle());
                    }
                } else {
                    // Behandle den Fall, dass keine Tracks in der DB gefunden wurden.
                    System.out.println("Keine Tracks gefunden.");
                }
            }
        });
    }
}
