package com.example.myapplication;

import java.util.List;

/**
 * Das Callback-Interface, das aufgerufen wird, wenn der Scan-Vorgang abgeschlossen ist.
 */
public interface LibraryScanCallback {
    void onScanCompleted(List<String> tracks);
}
