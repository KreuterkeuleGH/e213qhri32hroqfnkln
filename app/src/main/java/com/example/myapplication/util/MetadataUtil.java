package com.example.myapplication.util;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

public class MetadataUtil {
    private static final String TAG = "MetadataUtil";

    /**
     * Liest den Titel, den Interpret und die Dauer aus den Metadaten einer Audio-Datei aus.
     *
     * @param context  Der Context.
     * @param audioUri Die URI der Audio-Datei.
     * @return Ein Array mit drei Elementen: [0] = Titel, [1] = Interpret, [2] = Dauer in Millisekunden als String.
     */
    public static String[] getFullMetadata(Context context, Uri audioUri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(context, audioUri);
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            if (title == null || title.trim().isEmpty()) {
                title = "Unbekannt";
            }
            if (artist == null) {
                artist = "";
            }
            if (duration == null) {
                duration = "0";
            }
            return new String[]{title, artist, duration};
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Auslesen der Metadaten für: " + audioUri, e);
            return new String[]{"Unbekannt", "", "0"};
        } finally {
            try {
                mmr.release();
            } catch (Exception e) {
                Log.e(TAG, "Fehler beim Freigeben von MediaMetadataRetriever", e);
            }
        }
    }
}
