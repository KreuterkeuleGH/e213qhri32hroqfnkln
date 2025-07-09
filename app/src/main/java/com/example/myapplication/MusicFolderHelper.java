package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import androidx.preference.PreferenceManager;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility-Klasse, die den aktuell festgelegten Musikordner-URI liefert.
 * Zuerst wird der Intent geprüft, anschließend die SharedPreferences unter dem Schlüssel "music_folders".
 * Das Format der Einträge in "music_folders" ist "Name|URI". Der URI-Teil des ersten gültigen Eintrags wird zurückgegeben.
 */
public final class MusicFolderHelper {

    // Privater Konstruktor verhindert die Instanziierung dieser Utility-Klasse.
    private MusicFolderHelper() {}

    /**
     * Liefert den aktuell festgelegten Musikordner-URI. Zuerst wird der Intent der übergebenen Activity geprüft,
     * danach wird in den SharedPreferences unter dem Schlüssel "music_folders" nachgesehen.
     *
     * @param activity Die Activity, von der der Intent und die SharedPreferences abgerufen werden.
     * @return Der Musikordner-URI oder eine leere Zeichenkette, falls keiner gefunden wurde.
     */
    public static String getMusicFolderUri(Activity activity) {
        if (activity == null) {
            return "";
        }

        // Zuerst: Versuch, den URI aus dem Intent zu erhalten.
        Intent intent = activity.getIntent();
        String folderUri = intent != null ? intent.getStringExtra("folder_uri") : null;
        if (!TextUtils.isEmpty(folderUri)) {
            return folderUri;
        }

        // Falls nicht im Intent vorhanden, in den SharedPreferences nachsehen.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> folderSet = prefs.getStringSet("music_folders", new HashSet<>());
        if (folderSet != null && !folderSet.isEmpty()) {
            // Wähle den ersten gültigen Eintrag und gib den URI zurück.
            for (String entry : folderSet) {
                if (!TextUtils.isEmpty(entry)) {
                    String[] parts = entry.split("\\|");
                    if (parts.length >= 2 && !TextUtils.isEmpty(parts[1])) {
                        return parts[1];
                    }
                }
            }
        }
        // Keine gültige URI gefunden.
        return "";
    }
}
