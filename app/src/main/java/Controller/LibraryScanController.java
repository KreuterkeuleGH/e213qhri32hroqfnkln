package Controller;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LifecycleOwner;

import com.example.myapplication.util.MetadataUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * LibraryScanController verwaltet das gezielte Scannen eines Musikordners.
 * Mit der Methode scanLibrary(LifecycleOwner, String, LibraryScanCallback) wird der Inhalt
 * des ausgewählten Ordners (also dessen eigene Dateien und alle Dateien in allen Unterordnern)
 * gescannt und als Liste von Tracktiteln zurückgeliefert.
 */
public class LibraryScanController {

    private static final String TAG = "LibraryScanController";
    private Context context;

    public LibraryScanController(Context context) {
        // Verwende den ApplicationContext, um Memory-Leaks zu vermeiden.
        this.context = context.getApplicationContext();
    }

    /**
     * Callback-Interface, das aufgerufen wird, sobald der Scan abgeschlossen ist.
     */
    public interface LibraryScanCallback {
        void onLibraryScanned(List<String> tracks);
    }

    /**
     * Scannt rekursiv den Musikordner, der über den folderUri angegeben wird.
     * Es werden alle Dateien im Ordner (Wurzelverzeichnis) und in allen Unterordnern gefunden.
     * Dateien in übergeordneten Ordnern werden nicht berücksichtigt.
     *
     * @param lifecycleOwner Der LifecycleOwner (z. B. eine Activity) – dient hier zur Binding-Sicherheit.
     * @param folderUri      Der URI des zu scannenden Ordners.
     * @param callback       Das Callback, an das nach Abschluss des Scanvorgangs die Liste der gefundenen Tracktitel übergeben wird.
     */
    public void scanLibrary(final LifecycleOwner lifecycleOwner, final String folderUri, final LibraryScanCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> tracksCollected = new ArrayList<>();
                try {
                    Uri uri = Uri.parse(folderUri);
                    DocumentFile rootFolder = DocumentFile.fromTreeUri(context, uri);
                    if (rootFolder != null && rootFolder.exists() && rootFolder.isDirectory()) {
                        // Starte den Scan im gewählten Ordner – dies beinhaltet alle Dateien im Wurzelverzeichnis und in allen Unterordnern.
                        scanDirectoryRecursive(rootFolder, tracksCollected);
                    } else {
                        Log.w(TAG, "Der angegebene Ordner ist ungültig oder nicht verfügbar: " + folderUri);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Fehler beim Scannen des Ordners: " + folderUri, e);
                }
                // Ergebnis auf den Main-Thread posten
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLibraryScanned(tracksCollected);
                    }
                });
            }
        }).start();
    }

    /**
     * Durchläuft rekursiv den angegebenen Ordner und fügt alle gefundenen Audio-Dateien (aus dem aktuellen Ordner und allen Unterordnern)
     * in die Ergebnisliste ein.
     *
     * @param directory Der aktuell zu durchsuchende Ordner als DocumentFile.
     * @param collector Die Liste, in der die gefundenen Tracktitel gesammelt werden.
     */
    private void scanDirectoryRecursive(DocumentFile directory, List<String> collector) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }

        // Liste alle Dateien und Unterordner im aktuellen Verzeichnis
        DocumentFile[] files = directory.listFiles();
        for (DocumentFile file : files) {
            if (file.isFile()) {
                if (file.getName() != null && isAudioFile(file.getName())) {
                    String fileName = file.getName();
                    String[] metadata = MetadataUtil.getFullMetadata(context, file.getUri());
                    String title = metadata[0];
                    // Falls der ausgelesene Titel unbrauchbar ist, verwende den Dateinamen ohne Erweiterung als Fallback.
                    if (title == null || title.trim().isEmpty() || title.equalsIgnoreCase("Unbekannt")) {
                        int dotIndex = fileName.lastIndexOf('.');
                        title = (dotIndex != -1) ? fileName.substring(0, dotIndex).trim() : fileName;
                    }
                    collector.add(title);
                }
            } else if (file.isDirectory()) {
                // Rekursiver Aufruf für Unterordner
                scanDirectoryRecursive(file, collector);
            }
        }
    }

    /**
     * Prüft, ob der Dateiname auf eine bekannte Audio-Endung hinweist.
     *
     * @param fileName Der Name der Datei.
     * @return true, wenn es sich um eine Audiodatei handelt.
     */
    private boolean isAudioFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".mp3")
                || lower.endsWith(".wav")
                || lower.endsWith(".m4a")
                || lower.endsWith(".flac")
                || lower.endsWith(".ogg");
    }
}
