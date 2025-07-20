package com.example.myapplication.music.domain;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FolderManager {
    private static final String PREF_KEY_FOLDERS = "music_folders";
    private final SharedPreferences prefs;

    @Inject
    public FolderManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public boolean addFolder(Context context, Uri treeUri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, treeUri);
        String folderName = getFolderName(documentFile);
        String folderEntry = folderName + "|" + treeUri.toString();

        Set<String> folderSet = new HashSet<>(
                prefs.getStringSet(PREF_KEY_FOLDERS, new HashSet<>())
        );
        if (folderSet.add(folderEntry)) {
            prefs.edit()
                    .putStringSet(PREF_KEY_FOLDERS, folderSet)
                    .apply();
            return true;
        }
        return false;
    }

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

    private String getFolderName(DocumentFile documentFile) {
        if (documentFile != null && documentFile.getName() != null) {
            return documentFile.getName();
        }
        return "Unbekannt";
    }
}
