package com.example.myapplication;// Datei: TrackTest.java (im Ordner src/test/java/...)
import static org.junit.Assert.*;

import com.example.myapplication.music.data.Track;

import org.junit.Test;

public class TrackTest {

    @Test
    public void testEqualsAndHashCode() {
        // Erstelle zwei Tracks ohne gesetzte ID – standardmäßig ist id = 0.
        Track track1 = new Track("Title", "uri");
        Track track2 = new Track("Title", "uri");

        // Da beide id = 0 haben, sollten sie als gleich angesehen werden.
        assertTrue(track1.equals(track2));
        assertEquals(track1.hashCode(), track2.hashCode());

        // Setze unterschiedliche IDs und teste, dass sie jetzt ungleich sind.
        track1.setId(1);
        track2.setId(2);
        assertFalse(track1.equals(track2));
    }

    @Test
    public void testToString() {
        Track track = new Track("Sample Title", "sampleUri");
        String s = track.toString();
        assertTrue(s.contains("Sample Title"));
        assertTrue(s.contains("sampleUri"));
    }
}
