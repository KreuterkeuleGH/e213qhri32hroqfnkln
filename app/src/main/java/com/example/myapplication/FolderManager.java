package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import java.util.HashSet;
import java.util.Set;

public class FolderManager {
    private static final String PREF_KEY_FOLDERS = "music_folders";
    private SharedPreferences prefs;

    public FolderManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    /**
     * Fügt den Ordner hinzu, falls noch nicht vorhanden.
     * @param context Der Kontext zum Erstellen des DocumentFile.
     * @param treeUri Die URI des ausgewählten Ordners.
     * @return true, wenn der Ordner neu hinzugefügt wurde, sonst false.
     */
    public boolean addFolder(Context context, Uri treeUri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, treeUri);
        String folderName = getFolderName(documentFile);
        String folderEntry = folderName + "|" + treeUri.toString();

        Set<String> folderSet = new HashSet<>(prefs.getStringSet(PREF_KEY_FOLDERS, new HashSet<>()));
        if (folderSet.add(folderEntry)) {
            prefs.edit().putStringSet(PREF_KEY_FOLDERS, folderSet).apply();
            return true;
        }
        return false;
    }

    /**
     * Liest alle gespeicherten Ordner aus und erstellt eine Menge von FolderItem-Objekten.
     * @return Die Menge der FolderItem Objekte.
     */
    public Set<FolderItem> getFolderItems() {
        Set<FolderItem> folderItems = new HashSet<>();
        Set<String> folderSet = prefs.getStringSet(PREF_KEY_FOLDERS, new HashSet<>());
        for (String entry : folderSet) {
            String[] parts = entry.split("\\|");
            if (parts.length >= 2) {
                folderItems.add(new FolderItem(parts[0], parts[1]));
            }
        }
        return folderItems;
    }

    /**
     * Extrahiert den Namen des Ordners aus dem DocumentFile.
     * @param documentFile Das DocumentFile des Ordners.
     * @return Der Ordnername oder "Unbekannt", wenn nicht feststellbar.
     */
    private String getFolderName(DocumentFile documentFile) {
        if (documentFile != null && documentFile.getName() != null) {
            return documentFile.getName();
        }
        return "Unbekannt";
    }
}
