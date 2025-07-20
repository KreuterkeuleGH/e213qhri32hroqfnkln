package com.example.myapplication.music.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.local.TrackDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MusicRepository kapselt den Zugriff auf die native SQLite‑Datenbank über den TrackDatabaseHelper.
 * Es bietet sowohl synchrone Methoden als auch LiveData‑basierte Zugriffe.
 * Gleichzeitig wird ein Singleton-Muster in Verbindung mit einem ExecutorService genutzt,
 * um wiederholte Thread-Erstellungen zu vermeiden.
 */
public class MusicRepository {
    private static final String TAG = "MusicRepository";
    private static MusicRepository instance;
    // ExecutorService mit zwei Threads für Hintergrundaufgaben
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private TrackDatabaseHelper dbHelper;
    private final Object dbLock = new Object();

    // Privater Konstruktor für das Singleton.
    private MusicRepository(Context context) {
        dbHelper = new TrackDatabaseHelper(context.getApplicationContext());
        Log.d(TAG, "MusicRepository Instanz erstellt");
    }

    public static MusicRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (MusicRepository.class) {
                if (instance == null) {
                    instance = new MusicRepository(context);
                }
            }
        }
        return instance;
    }

    /**
     * Liefert ein LiveData-Objekt, das asynchron mit der vollständigen Trackliste befüllt wird.
     *
     * @return LiveData, das eine Liste von Track-Objekten enthält.
     */
    public LiveData<List<Track>> getAllTracksLiveData() {
        final MutableLiveData<List<Track>> liveData = new MutableLiveData<>();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Track> tracks = getCachedTracks();
                liveData.postValue(tracks);
            }
        });
        return liveData;
    }

    /**
     * Liest alle Tracks synchron aus der Datenbank.
     *
     * @return Eine Liste aller gespeicherten Tracks, die nicht als gelöscht markiert sind.
     */
    public List<Track> getCachedTracks() {
        Log.d(TAG, "=== getCachedTracks() gestartet ===");
        List<Track> tracks = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "Datenbank erfolgreich geöffnet");

            String[] columns = {
                    TrackDatabaseHelper.COLUMN_ID,
                    TrackDatabaseHelper.COLUMN_TITLE,
                    TrackDatabaseHelper.COLUMN_URI,
                    TrackDatabaseHelper.COLUMN_ARTIST   // falls die Migration durchgeführt wurde
            };

            // Änderung: Nur Tracks laden, die nicht als gelöscht markiert sind (deleted = 0)
            String selection = "deleted = ?";
            String[] selectionArgs = new String[]{"0"};

            cursor = db.query(TrackDatabaseHelper.TABLE_TRACKS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                int trackCount = cursor.getCount();
                Log.d(TAG, "Anzahl Tracks in DB: " + trackCount);

                int index = 0;
                while (cursor.moveToNext()) {
                    try {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_TITLE));
                        String uri = cursor.getString(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_URI));

                        // Artist optional lesen
                        String artist = null;
                        try {
                            int artistColumnIndex = cursor.getColumnIndex(TrackDatabaseHelper.COLUMN_ARTIST);
                            if (artistColumnIndex != -1) {
                                artist = cursor.getString(artistColumnIndex);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Artist-Spalte nicht verfügbar", e);
                        }

                        Track track = new Track(title, uri);
                        track.setId(id);
                        if (artist != null) {
                            track.setArtist(artist); // Falls diese Methode existiert
                        }
                        tracks.add(track);
                        index++;
                    } catch (Exception e) {
                        Log.e(TAG, "Fehler beim Lesen von Track " + index, e);
                    }
                }
            } else {
                Log.e(TAG, "Cursor ist NULL!");
            }

        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Zugriff auf Datenbank", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        Log.d(TAG, "=== getCachedTracks() beendet - " + tracks.size() + " Tracks geladen ===");
        return tracks;
    }


    /**
     * Liest synchron eine Seite von Tracks aus der Datenbank.
     * Es werden nur Tracks zurückgegeben, die nicht als gelöscht markiert sind (deleted = 0).
     *
     * @param page             Die zu ladende Seite (beginnend bei 0).
     * @param pageSize         Anzahl der Tracks pro Seite.
     * @param folderUriFilter  Optionale Filterung: Es werden nur Tracks geliefert, deren URI mit dem Filter beginnen.
     * @return Eine Liste von Track-Objekten.
     */
    public List<Track> getCachedTracksPage(int page, int pageSize, String folderUriFilter) {
        Log.d(TAG, "getCachedTracksPage: page=" + page + ", pageSize=" + pageSize + ", filter=" + folderUriFilter);

        List<Track> tracks = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // Basis: Nur Tracks, die nicht als gelöscht markiert sind.
            String selection = "deleted = 0";
            String[] selectionArgs = null;

            // Wenn ein Filter gesetzt ist, erweitern wir die WHERE-Klausel.
            if (folderUriFilter != null && !folderUriFilter.trim().isEmpty()) {
                selection += " AND " + TrackDatabaseHelper.COLUMN_URI + " LIKE ?";
                selectionArgs = new String[]{ folderUriFilter + "%" };
            }

            String limitClause = pageSize + " OFFSET " + (page * pageSize);
            String[] columns = {
                    TrackDatabaseHelper.COLUMN_ID,
                    TrackDatabaseHelper.COLUMN_TITLE,
                    TrackDatabaseHelper.COLUMN_URI,
                    TrackDatabaseHelper.COLUMN_ARTIST
            };

            cursor = db.query(TrackDatabaseHelper.TABLE_TRACKS, columns, selection, selectionArgs, null, null, null, limitClause);
            if (cursor != null) {
                while (cursor.moveToNext()){
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_TITLE));
                    String uri = cursor.getString(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_URI));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(TrackDatabaseHelper.COLUMN_ARTIST));
                    Track track = new Track(title, uri, artist);
                    track.setId(id);
                    tracks.add(track);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler in getCachedTracksPage", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        Log.d(TAG, "getCachedTracksPage zurückgegeben: " + tracks.size() + " Tracks");
        return tracks;
    }


    /**
     * Fügt eine Liste von Tracks in die Datenbank ein, überspringt dabei exakte Dubletten.
     * Vor dem Einfügen wird sichergestellt, dass jeder neue Track als nicht gelöscht (deleted = 0)
     * markiert wird.
     *
     * @param tracks Die Liste der neuen Track-Objekte.
     */
    public void insertTracks(List<Track> tracks) {
        Log.d(TAG, "=== insertTracks() gestartet ===");
        Log.d(TAG, "Einzufügende Tracks: " + tracks.size());

        if (tracks == null || tracks.isEmpty()) {
            Log.w(TAG, "Keine Tracks zum Einfügen!");
            return;
        }

        SQLiteDatabase db = null;
        int successCount = 0;
        int errorCount = 0;

        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            Log.d(TAG, "Transaktion gestartet");

            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                try {
                    // Prüfe, ob der Track bereits existiert (exakte Übereinstimmung mit Titel, Artist und URI, deleted = 0)
                    if (trackExists(db, track)) {
                        Log.d(TAG, "Track bereits vorhanden (überspringe): " + track.getTitle());
                        continue;
                    }
                    ContentValues values = new ContentValues();
                    values.put(TrackDatabaseHelper.COLUMN_TITLE, track.getTitle());
                    values.put(TrackDatabaseHelper.COLUMN_URI, track.getUri());
                    values.put(TrackDatabaseHelper.COLUMN_ARTIST, track.getArtist());
                    // Neuen Track als nicht gelöscht markieren (deleted = 0)
                    values.put(TrackDatabaseHelper.COLUMN_DELETED, 0);

                    long result = db.insert(TrackDatabaseHelper.TABLE_TRACKS, null, values);
                    if (result != -1) {
                        successCount++;
                        Log.d(TAG, "Track " + (i + 1) + " eingefügt: " + track.getTitle());
                    } else {
                        errorCount++;
                        Log.e(TAG, "Fehler beim Einfügen von Track " + (i + 1) + ": " + track.getTitle());
                    }
                } catch (Exception e) {
                    errorCount++;
                    Log.e(TAG, "Exception beim Einfügen von Track " + (i + 1), e);
                }
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Transaktion erfolgreich abgeschlossen");
        } catch (Exception e) {
            Log.e(TAG, "Kritischer Fehler in insertTracks", e);
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e) {
                    Log.e(TAG, "Fehler beim Schließen der DB", e);
                }
            }
        }

        Log.d(TAG, "=== insertTracks() beendet ===");
        Log.d(TAG, "Erfolgreich eingefügt: " + successCount);
        Log.d(TAG, "Fehler: " + errorCount);

        // Verifikation: Prüfe, ob die Gesamtzahl in der Datenbank stimmt.
        List<Track> allTracks = getCachedTracks();
        Log.d(TAG, "Verifikation: Insgesamt " + allTracks.size() + " Tracks in DB");
    }



    /**
     * Löscht alle Einträge in der Track-Tabelle.
     */
    public void deleteAllTracks() {
        Log.d(TAG, "=== deleteAllTracks() gestartet ===");
        synchronized (dbLock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                int deletedRows = db.delete(TrackDatabaseHelper.TABLE_TRACKS, null, null);
                Log.d(TAG, "Gelöschte Tracks: " + deletedRows);
            } catch (Exception e) {
                Log.e(TAG, "Fehler beim Löschen aller Tracks", e);
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
        Log.d(TAG, "=== deleteAllTracks() beendet ===");
    }

    private boolean trackExists(SQLiteDatabase db, Track track) {
        String query = "SELECT COUNT(*) FROM " + TrackDatabaseHelper.TABLE_TRACKS +
                " WHERE " + TrackDatabaseHelper.COLUMN_TITLE + " = ? AND " +
                TrackDatabaseHelper.COLUMN_ARTIST + " = ? AND " +
                TrackDatabaseHelper.COLUMN_URI + " = ? AND deleted = 0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{track.getTitle(), track.getArtist(), track.getUri()});
            if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Überprüfen, ob Track existiert", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }


    public void markTracksDeletedByFolder(String folderUri) {
        Log.d(TAG, "Markiere Tracks als gelöscht für Ordner: " + folderUri);
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            // Aktualisiert alle Einträge, deren URI mit folderUri beginnt.
            db.execSQL("UPDATE " + TrackDatabaseHelper.TABLE_TRACKS +
                            " SET deleted = 1 WHERE " + TrackDatabaseHelper.COLUMN_URI + " LIKE ?",
                    new String[]{folderUri + "%"});
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Markieren gelöschter Tracks", e);
        } finally {
            if (db != null) db.close();
        }
    }

    public void cleanupDeletedTracks() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            // Zähle gelöschte Tracks
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TrackDatabaseHelper.TABLE_TRACKS +
                    " WHERE deleted = 1", null);
            int count = 0;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
                cursor.close();
            }
            Log.d(TAG, "Anzahl gelöschter Tracks: " + count);
            if (count >= 10) {
                // Lösche endgültig die als gelöscht markierten Einträge
                db.execSQL("DELETE FROM " + TrackDatabaseHelper.TABLE_TRACKS +
                        " WHERE deleted = 1");
                Log.d(TAG, "Bereinigung der gelöschten Tracks durchgeführt.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler bei der Bereinigung gelöschter Tracks", e);
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Markiert alle Tracks, deren URI nicht mit einem der gültigen Ordner-URIs beginnt, als gelöscht.
     *
     * @param validFolderUris Kommagetrennte gültige Ordner-URIs.
     */
    public void cleanupTracks(String validFolderUris) {
        Log.d(TAG, "=== cleanupTracks() gestartet ===");
        synchronized (dbLock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                if (validFolderUris == null || validFolderUris.trim().isEmpty()) {
                    ContentValues cvAll = new ContentValues();
                    cvAll.put("deleted", 1);
                    int countAll = db.update(TrackDatabaseHelper.TABLE_TRACKS, cvAll, "deleted = 0", null);
                    Log.d(TAG, "Keine gültigen Ordner. Alle Tracks als gelöscht markiert: " + countAll);
                    return;
                }
                String[] validFolders = validFolderUris.split(",");
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append("deleted = 0 AND NOT (");
                for (int i = 0; i < validFolders.length; i++) {
                    String folder = validFolders[i].trim();
                    if (i > 0) {
                        conditionBuilder.append(" OR ");
                    }
                    conditionBuilder.append(TrackDatabaseHelper.COLUMN_URI)
                            .append(" LIKE '")
                            .append(folder)
                            .append("%'");
                }
                conditionBuilder.append(")");
                String condition = conditionBuilder.toString();
                Log.d(TAG, "cleanupTracks WHERE-Bedingung: " + condition);
                ContentValues cv = new ContentValues();
                cv.put("deleted", 1);
                int updatedRows = db.update(TrackDatabaseHelper.TABLE_TRACKS, cv, condition, null);
                Log.d(TAG, "Bereinigung durchgeführt, aktualisierte Zeilen: " + updatedRows);
            } catch (Exception e) {
                Log.e(TAG, "Fehler bei cleanupTracks", e);
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
        Log.d(TAG, "=== cleanupTracks() beendet ===");
    }


    public void debugDatabase() {
        Log.d(TAG, "=== Database Debug ===");
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getReadableDatabase();

            // Prüfen ob Tabelle existiert
            Cursor tableCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TrackDatabaseHelper.TABLE_TRACKS + "'", null);
            if (tableCursor.moveToFirst()) {
                Log.d(TAG, "Tabelle '" + TrackDatabaseHelper.TABLE_TRACKS + "' existiert");
            } else {
                Log.e(TAG, "Tabelle '" + TrackDatabaseHelper.TABLE_TRACKS + "' existiert NICHT!");
            }
            tableCursor.close();

            // Spalten der Tabelle anzeigen
            Cursor columnCursor = db.rawQuery("PRAGMA table_info(" + TrackDatabaseHelper.TABLE_TRACKS + ")", null);
            Log.d(TAG, "Spalten in der Tabelle:");
            while (columnCursor.moveToNext()) {
                String columnName = columnCursor.getString(1);
                String columnType = columnCursor.getString(2);
                Log.d(TAG, "  - " + columnName + " (" + columnType + ")");
            }
            columnCursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Database Debug", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}