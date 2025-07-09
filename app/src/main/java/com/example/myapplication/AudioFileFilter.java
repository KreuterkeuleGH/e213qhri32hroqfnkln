package com.example.myapplication;

import androidx.documentfile.provider.DocumentFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Eine Utility-Klasse, die unterstützt, Audio-Dateien basierend auf MIME-Typ oder Dateierweiterung zu filtern.
 * Diese Klasse gehört zum Model und enthält keine UI-spezifische Logik.
 */
public final class AudioFileFilter {

    private static final Set<String> SUPPORTED_MIME_TYPES = new HashSet<>(Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/aac", "audio/ogg", "audio/wav",
            "audio/flac", "audio/mp4", "audio/amr", "audio/midi", "audio/x-midi",
            "audio/alac", "audio/x-alac", "audio/ape", "audio/x-ape", "audio/opus",
            "audio/x-ms-wma", "audio/dsd", "audio/3gpp", "audio/3gpp2"
    ));

    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp3", ".aac", ".ogg", ".wav", ".flac", ".m4a", ".amr", ".mid",
            ".midi", ".alac", ".ape", ".opus", ".wma", ".3gp", ".3gpp", ".3gpp2"
    ));

    // Privater Konstruktor verhindert Instanziierung
    private AudioFileFilter() {}

    /**
     * Prüft, ob der MIME-Typ der Datei in der Liste der unterstützten Typen enthalten ist.
     *
     * @param file Das zu prüfende DokumentFile.
     * @return true, falls der MIME-Typ unterstützt wird.
     */
    public static boolean isSupportedMimeType(DocumentFile file) {
        if (file == null) return false;
        String mimeType = file.getType();
        return mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Prüft, ob die Dateiendung des Dateinamens in der Liste der unterstützten Erweiterungen enthalten ist.
     *
     * @param file Das zu prüfende DocumentFile.
     * @return true, falls die Dateiendung unterstützt wird.
     */
    public static boolean isSupportedExtension(DocumentFile file) {
        if (file == null) return false;
        String fileName = file.getName();
        if (fileName == null) return false;
        fileName = fileName.toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bestimmt, ob es sich um eine unterstützte Audio-Datei handelt.
     *
     * @param file Das zu prüfende DocumentFile.
     * @return true, falls entweder der MIME-Typ oder die Erweiterung unterstützt wird.
     */
    public static boolean isSupportedAudioFile(DocumentFile file) {
        return isSupportedMimeType(file) || isSupportedExtension(file);
    }

    /**
     * Lädt alle unterstützten Audio-Dateien aus einem angegebenen Ordner.
     *
     * @param folder Das Ordner-DokumentFile, das durchsucht werden soll.
     * @return Eine Liste der unterstützten Audio-Dateien.
     */
    public static List<DocumentFile> loadSupportedAudioFiles(DocumentFile folder) {
        List<DocumentFile> supportedFiles = new ArrayList<>();
        if (folder == null || !folder.isDirectory()) return supportedFiles;
        for (DocumentFile file : folder.listFiles()) {
            if (file.isFile() && isSupportedAudioFile(file)) {
                supportedFiles.add(file);
            }
        }
        return supportedFiles;
    }
}
