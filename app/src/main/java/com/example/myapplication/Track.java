package com.example.myapplication;

import java.util.Objects;

/**
 * Die Track-Klasse repräsentiert einen Musiktrack.
 * Sie gehört zum Model und enthält nur die Daten (ID, Titel, URI, Artist und Dauer) als POJO.
 */
public class Track {
    private int id; // Auto-generierter Primärschlüssel
    private String title;
    private String uri;
    private String artist;
    private String duration; // Dauer im Format "mm:ss"

    /**
     * Konstruktor ohne Dauer, setzt den Artist standardmäßig auf "Unbekannter Künstler" und die Dauer auf "00:00".
     */
    public Track(String title, String uri) {
        this.title = title;
        this.uri = uri;
        this.artist = "Unbekannter Künstler";
        this.duration = "00:00";
    }

    /**
     * Konstruktor ohne Dauer, setzt den Artist (falls null auf "Unbekannter Künstler") und die Dauer auf "00:00".
     */
    public Track(String title, String uri, String artist) {
        this.title = title;
        this.uri = uri;
        this.artist = artist != null ? artist : "Unbekannter Künstler";
        this.duration = "00:00";
    }

    /**
     * Konstruktor mit Dauer, setzt alle Parameter.
     */
    public Track(String title, String uri, String artist, String duration) {
        this.title = title;
        this.uri = uri;
        this.artist = artist != null ? artist : "Unbekannter Künstler";
        this.duration = duration != null ? duration : "00:00";
    }

    // Getter und Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track)) return false;
        Track track = (Track) o;
        return id == track.id &&
                Objects.equals(title, track.title) &&
                Objects.equals(uri, track.uri) &&
                Objects.equals(artist, track.artist) &&
                Objects.equals(duration, track.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, uri, artist, duration);
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", uri='" + uri + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
