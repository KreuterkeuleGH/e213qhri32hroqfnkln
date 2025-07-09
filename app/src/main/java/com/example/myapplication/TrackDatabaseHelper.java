package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * TrackDatabaseHelper verwaltet die native SQLite‑Datenbank.
 * Bei einem Upgrade führen wir hier eine Migration durch, statt die Tabelle komplett neu zu erstellen.
 */
public class TrackDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tracks.db";
    // Erhöhte Version auf 3, um die neue Spalte "deleted" zu integrieren.
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_TRACKS = "tracks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_ARTIST = "artist";
    // Neue Spalte, um den Löschstatus eines Tracks zu markieren
    public static final String COLUMN_DELETED = "deleted";

    // SQL-Befehl zum Erstellen der Tabelle in der Version 3
    private static final String DATABASE_CREATE =
            "CREATE TABLE " + TABLE_TRACKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_URI + " TEXT NOT NULL, " +
                    COLUMN_ARTIST + " TEXT DEFAULT '', " +
                    COLUMN_DELETED + " INTEGER DEFAULT 0" +
                    ");";

    public TrackDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Aktivierung von Write-Ahead Logging (WAL) in onConfigure()
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    /**
     * Anstatt die Tabelle beim Upgrade komplett zu löschen – was alle vorhandenen Daten entfernt –
     * führen wir hier eine Migration durch, indem wir zum Beispiel neue Spalten hinzufügen.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration von Version 1 auf Version 2: Spalte "artist" hinzufügen (falls noch nicht vorhanden)
        if (oldVersion < 2) {
            String alterTableArtist = "ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN " + COLUMN_ARTIST + " TEXT DEFAULT '';";
            db.execSQL(alterTableArtist);
        }
        // Migration von Version 2 auf Version 3: Spalte "deleted" hinzufügen
        if (oldVersion < 3) {
            String alterTableDeleted = "ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN " + COLUMN_DELETED + " INTEGER DEFAULT 0;";
            db.execSQL(alterTableDeleted);
        }
        
    }
}
