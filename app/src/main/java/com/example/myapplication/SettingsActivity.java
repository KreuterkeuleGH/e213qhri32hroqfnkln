package com.example.myapplication;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Die SettingsActivity ermöglicht es dem Nutzer, Musikordner auszuwählen,
 * deren persistente Zugriffsberechtigungen zu übernehmen und eine Übersicht der gespeicherten Ordner anzuzeigen.
 * Beim langen Klick auf einen Ordner wird eine detaillierte View (z. B. TitleListActivity) gestartet.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;
    private Button btnSelectMusicFolder;
    private SharedPreferences prefs;
    private ListView listViewFolders;
    private List<FolderItem> folderItemList;
    private FolderAdapter folderAdapter;
    private FolderManager folderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-Edge-Anpassung (falls noch gewünscht)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        setupWindowInsets();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        folderManager = new FolderManager(prefs);

        btnSelectMusicFolder = findViewById(R.id.btnSelectMusicFolder);
        btnSelectMusicFolder.setOnClickListener(v -> openFolderPicker());

        listViewFolders = findViewById(R.id.listViewFolders);
        folderItemList = new ArrayList<>();

        folderAdapter = new FolderAdapter(this, folderItemList,
                (folder) -> removeFolder(folder),
                (folder) -> {
                    Intent intent = new Intent(SettingsActivity.this, TitleListActivity.class);
                    intent.putExtra("folder_name", folder.getName());
                    intent.putExtra("folder_uri", folder.getUri());
                    startActivity(intent);
                }
        );
        listViewFolders.setAdapter(folderAdapter);

        displayFolderList();
    }

    /**
     * Passt das Layout an die Systemleisten an (Edge-to-Edge).
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.musik_settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Öffnet den System-Ordner-Picker, sodass der Nutzer einen Musikordner auswählen kann.
     */
    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
    }

    /**
     * Wird aufgerufen, wenn der Ordner-Picker ein Ergebnis liefert.
     */
    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE
                && resultCode == Activity.RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                int computedFlags = data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                int takeFlags = (computedFlags == 0) ? Intent.FLAG_GRANT_READ_URI_PERMISSION : computedFlags;
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                processFolderResult(treeUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Verarbeitet den ausgewählten Ordner:
     * - Speichert den Ordner (via FolderManager) in SharedPreferences
     * - Aktualisiert die Ordnerliste und triggert einen OneTime-Scan, um den neuen Ordner zeitnah einzuscannen.
     */
    private void processFolderResult(Uri treeUri) {
        boolean added = folderManager.addFolder(this, treeUri);
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, treeUri);
        String folderName = (documentFile != null && documentFile.getName() != null)
                ? documentFile.getName() : "Unbekannt";

        if (added) {
            Toast.makeText(this, "Musikordner " + folderName + " hinzugefügt", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Musikordner " + folderName + " existiert bereits", Toast.LENGTH_SHORT).show();
        }
        displayFolderList();
        // Nach dem Hinzufügen eines neuen Ordners sofort einen Scan triggern.
        triggerMusicScan();
    }

    /**
     * Aktualisiert den FolderAdapter mit den in SharedPreferences gespeicherten Ordnern.
     */
    private void displayFolderList() {
        folderItemList.clear();
        folderItemList.addAll(folderManager.getFolderItems());
        folderAdapter.notifyDataSetChanged();
    }

    /**
     * Entfernt den angegebenen Ordner aus SharedPreferences, markiert alle zugehörigen Tracks als gelöscht
     * und aktualisiert die Anzeige.
     */
    private void removeFolder(FolderItem item) {
        Set<String> folderSet = new HashSet<>(prefs.getStringSet("music_folders", new HashSet<>()));
        String entry = item.getName() + "|" + item.getUri();
        if (folderSet.remove(entry)) {
            prefs.edit().putStringSet("music_folders", folderSet).apply();
            Toast.makeText(this, "Ordner " + item.getName() + " entfernt", Toast.LENGTH_SHORT).show();
            // Markiere alle Tracks des entfernten Ordners als gelöscht
            MusicRepository.getInstance(this).markTracksDeletedByFolder(item.getUri());
            displayFolderList();
            // Optional: Hier könntest du einen CleanupWorker enqueuen, um die DB von als gelöscht markierten Tracks zu bereinigen.
        } else {
            Toast.makeText(this, "Ordner " + item.getName() + " konnte nicht entfernt werden.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Triggert einen OneTimeWorkRequest für den MusicLoaderWorker, um einen sofortigen Scan zu starten.
     */
    private void triggerMusicScan() {
        OneTimeWorkRequest scanRequest = new OneTimeWorkRequest.Builder(MusicLoaderWorker.class).build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork("MusicLoaderOnSettings", ExistingWorkPolicy.REPLACE, scanRequest);
    }
}
