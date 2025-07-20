package com.example.myapplication;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MusicRepositoryTest {

    private MusicRepository repository;
    private Context context;
    // Wird als Filter übergeben. Ein leerer String bedeutet "kein Filter".
    private static final String NO_FILTER = "";

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Zugriff über die Singleton-Methode, da der Konstruktor privat ist.
        repository = MusicRepository.getInstance(context);
        // Vor jedem Test die Datenbank leeren.
        repository.deleteAllTracks();
    }

    @Test
    public void testInsertAndGetCachedTracks() {
        // Erstelle beispielhafte Tracks und füge sie ein.
        Track track1 = new Track("Song A", "uri://songA");
        Track track2 = new Track("Song B", "uri://songB");
        List<Track> trackList = Arrays.asList(track1, track2);
        repository.insertTracks(trackList);

        // Lies alle Tracks aus.
        List<Track> cachedTracks = repository.getCachedTracks();
        assertNotNull(cachedTracks);
        assertEquals(2, cachedTracks.size());
        // Überprüfe, ob die Titel übereinstimmen.
        assertEquals("Song A", cachedTracks.get(0).getTitle());
        assertEquals("Song B", cachedTracks.get(1).getTitle());
    }

    @Test
    public void testDeleteAllTracks() {
        // Füge einen Track ein, lösche dann alle und prüfe das Ergebnis.
        Track track = new Track("Song A", "uri://songA");
        repository.insertTracks(Collections.singletonList(track));
        repository.deleteAllTracks();
        List<Track> cachedTracks = repository.getCachedTracks();
        assertTrue(cachedTracks.isEmpty());
    }

    @Test
    public void testGetCachedTracksPage() {
        // Füge 10 Tracks ein.
        List<Track> tracks = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            tracks.add(new Track("Track " + i, "uri://track" + i));
        }
        repository.insertTracks(tracks);

        // Hole Seite 0 mit pageSize 5, ohne Filter (leeren String als Filter)
        List<Track> page0 = repository.getCachedTracksPage(0, 5, NO_FILTER);
        assertEquals(5, page0.size());

        // Hole Seite 1 mit pageSize 5.
        List<Track> page1 = repository.getCachedTracksPage(1, 5, NO_FILTER);
        assertEquals(5, page1.size());

        // Hole Seite 2 – sollte leer sein.
        List<Track> page2 = repository.getCachedTracksPage(2, 5, NO_FILTER);
        assertTrue(page2.isEmpty());
    }
}
