package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MusicLoaderWorker extends Worker {

    private static final String TAG = "MusicLoaderWorker";

    public MusicLoaderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Lese die Input-Daten (sicherstellen, dass echte URIs übergeben werden)
            String folderUrisInput = getInputData().getString("folder_uris");
            int batchSize = getInputData().getInt("page_size", 50);

            Log.d(TAG, "=== MusicLoaderWorker gestartet ===");
            Log.d(TAG, "Input folder_uris: " + folderUrisInput);
            Log.d(TAG, "Batch-Größe (page_size): " + batchSize);

            MusicRepository repository = MusicRepository.getInstance(getApplicationContext());

            // Wenn keine Folder-URIs übergeben wurden, lösche alle Tracks und gebe ein leeres Ergebnis zurück.
            if (folderUrisInput == null || folderUrisInput.trim().isEmpty()) {
                Log.d(TAG, "Keine Folder-URIs übergeben. Leere DB.");
                repository.deleteAllTracks();
                Data outputData = new Data.Builder().putString("tracks", "[]").build();
                return Result.success(outputData);
            }

            // Bereinige die DB: Lösche alle Tracks, die nicht zu den aktuell gültigen Foldern gehören.
            repository.cleanupTracks(folderUrisInput);

            String[] folderUris = folderUrisInput.split(",");
            Log.d(TAG, "Anzahl zu scannender Ordner: " + folderUris.length);

            List<String> allTrackTitles = new ArrayList<>();

            // Optionale Ausgabe: Anzahl der bestehenden Tracks vor dem Scan
            List<Track> existingTracks = repository.getCachedTracks();
            Log.d(TAG, "Tracks bereits in DB vor Scan: " + existingTracks.size());

            // Iteriere über alle Folder-URIs und verarbeite jeden Ordner in Batches
            for (String folderUriString : folderUris) {
                folderUriString = folderUriString.trim();
                if (folderUriString.isEmpty()) continue;

                Log.d(TAG, "Verarbeite Ordner (Batch): " + folderUriString);
                Uri folderUri = Uri.parse(folderUriString);
                processFolderInBatches(folderUri, getApplicationContext(), repository, batchSize);
            }

            // Finale Überprüfung der DB: Alle gültigen (nicht gelöschten) Tracks abrufen.
            List<Track> cachedTracks = repository.getCachedTracks();
            Log.d(TAG, "=== Finale Anzahl Tracks in DB: " + cachedTracks.size() + " ===");

            for (Track track : cachedTracks) {
                if (track.getTitle() != null) {
                    allTrackTitles.add(track.getTitle().trim());
                }
            }

            JSONArray jsonArrayFinal = new JSONArray(allTrackTitles);
            String finalTracks = jsonArrayFinal.toString();
            Log.d(TAG, "Zurückgegebene Track-Titel: " + finalTracks);

            Data outputData = new Data.Builder()
                    .putString("tracks", finalTracks)
                    .build();
            return Result.success(outputData);
        } catch (Exception e) {
            Log.e(TAG, "FEHLER in doWork", e);
            return Result.failure();
        }
    }

    /**
     * Verarbeitet den angegebenen Ordner in Batches (Seiten) und fügt die gefundenen Tracks stapelweise in die Datenbank ein.
     *
     * @param folderUri  Die URI des Ordners.
     * @param context    Der Context.
     * @param repository Die MusicRepository-Instanz.
     * @param batchSize  Die maximale Batch-Größe (z. B. 50 Tracks pro Batch).
     */
    private void processFolderInBatches(Uri folderUri, Context context, MusicRepository repository, int batchSize) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, folderUri);
        if (pickedDir == null || !pickedDir.exists() || !pickedDir.isDirectory()) {
            Log.e(TAG, "Ungültiger Ordner: " + folderUri.toString());
            return;
        }
        // Lade alle unterstützten Audio-Dateien aus dem Ordner.
        // Hinweis: Hier wird idealerweise nur der Inhalt des ausgewählten Ordners verarbeitet.
        // Falls du eine Tiefensuche in Unterordnern (von der DB-Seite) wünschst,
        // sollte diese Logik im Repository oder über separate Mechanismen erfolgen.
        List<DocumentFile> audioFiles = AudioFileFilter.loadSupportedAudioFiles(pickedDir);
        int totalFiles = audioFiles.size();
        Log.d(TAG, "Ordner " + folderUri.toString() + " enthält " + totalFiles + " Audio-Dateien.");

        int batchCount = 0;
        // Batchweise Verarbeitung
        for (int i = 0; i < totalFiles; i += batchSize) {
            int end = Math.min(totalFiles, i + batchSize);
            List<Track> batchTracks = new ArrayList<>();
            // Pro Batch: Dateien von i bis end verarbeiten
            for (int j = i; j < end; j++) {
                DocumentFile audioFile = audioFiles.get(j);
                if (audioFile == null || !audioFile.isFile() || audioFile.getName() == null) continue;
                String fileName = audioFile.getName();
                Log.d(TAG, "Batch-Datei: " + fileName);

                // Lese die Metadaten aus der Audio-Datei: Titel, Artist und Dauer (in Millisekunden)
                String[] meta = MetadataUtil.getFullMetadata(context, audioFile.getUri());
                String title = meta[0];
                String artist = meta[1];
                String rawDuration = meta[2];

                // Falls der Titel unbrauchbar ist, verwende den Dateinamen ohne Erweiterung als Fallback.
                if (title == null || title.trim().isEmpty() || "Unbekannt".equalsIgnoreCase(title)) {
                    int dotIndex = fileName.lastIndexOf('.');
                    title = (dotIndex != -1) ? fileName.substring(0, dotIndex).trim() : fileName;
                }
                // Konvertiere die Dauer in mm:ss
                String durationFormatted = formatDuration(rawDuration);

                // Erstelle das Track-Objekt und fülle es mit den ausgelesenen Werten.
                Track track = new Track(title, audioFile.getUri().toString(), artist);
                track.setDuration(durationFormatted);
                batchTracks.add(track);
            }
            // Füge den aktuellen Batch in die Datenbank ein.
            if (!batchTracks.isEmpty()) {
                repository.insertTracks(batchTracks);
                batchCount++;
                Log.d(TAG, "Batch " + batchCount + " eingefügt: " + batchTracks.size() + " Tracks.");
            }
        }
        Log.d(TAG, "Abschluss folder " + folderUri.toString() + ": " + batchCount + " Batches verarbeitet.");
    }

    /**
     * Wandelt die Dauer (als String in Millisekunden) in ein mm:ss-Format um.
     *
     * @param durationMs Dauer in Millisekunden als String.
     * @return Formatiert als "mm:ss", z. B. "03:45". Bei Fehlern wird "00:00" zurückgegeben.
     */
    private String formatDuration(String durationMs) {
        try {
            long ms = Long.parseLong(durationMs);
            long seconds = ms / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } catch (Exception e) {
            return "00:00";
        }
    }

    /**
     * Extrahiert den Titel und den Artist aus einem Dateinamen als Fallback,
     * wenn keine brauchbaren Metadaten vorhanden sind.
     * Hierbei wird der letzte Bindestrich als Trenner verwendet.
     *
     * Beispiel: "Bandname - Songtitel - Liveversion.wav" führt zu:
     *   • Artist: "Bandname - Songtitel"
     *   • Title: "Liveversion"
     *
     * @param fileName Der Original-Dateiname der Audio-Datei.
     * @return Ein Array, wobei [0] der Titel und [1] der Artist ist.
     */
    private String[] extractTitleAndArtist(String fileName) {
        // Entferne die Dateiendung, sofern vorhanden
        String baseName = fileName;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex).trim();
        }

        // Nutze den letzten Bindestrich als Trenner, falls vorhanden
        if (baseName.contains("-")) {
            int lastDash = baseName.lastIndexOf('-');
            String artist = baseName.substring(0, lastDash).trim();
            String title = baseName.substring(lastDash + 1).trim();
            if (title.isEmpty()) {
                title = baseName;
            }
            return new String[]{title, artist};
        } else {
            return new String[]{baseName, ""};
        }
    }
}
