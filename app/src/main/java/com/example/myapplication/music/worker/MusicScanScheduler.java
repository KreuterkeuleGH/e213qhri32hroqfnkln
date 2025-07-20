package com.example.myapplication.music.worker;

import android.app.Application;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.music.domain.FolderItem;
import com.example.myapplication.music.domain.FolderManager;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MusicScanScheduler {

    private static final String UNIQUE_PERIODIC_SCAN = "unique_periodic_scan";
    private static final String UNIQUE_ONETIME_SCAN  = "unique_onetime_scan";

    private final WorkManager workManager;
    private final int pageSize;
    private final FolderManager folderManager;

    @Inject
    public MusicScanScheduler(
            Application app,
            @Named("batch_size") int pageSize,
            FolderManager folderManager
    ) {
        this.workManager   = WorkManager.getInstance(app);
        this.pageSize      = pageSize;
        this.folderManager = folderManager;
    }

    public void schedulePeriodic() {
        String csv = buildFolderCsv();
        Data data = new Data.Builder()
                .putInt("page_size", pageSize)
                .putString("folder_uris", csv)
                .build();

        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                MusicLoaderWorker.class,
                15, TimeUnit.MINUTES)
                .addTag(UNIQUE_PERIODIC_SCAN)
                .setInputData(data)
                .build();

        workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_SCAN,
                ExistingPeriodicWorkPolicy.REPLACE,
                req
        );
    }

    public void triggerOneTime() {
        String csv = buildFolderCsv();
        Data data = new Data.Builder()
                .putInt("page_size", pageSize)
                .putString("folder_uris", csv)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(MusicLoaderWorker.class)
                .addTag(UNIQUE_ONETIME_SCAN)
                .setInputData(data)
                .build();

        workManager.enqueueUniqueWork(
                UNIQUE_ONETIME_SCAN,
                ExistingWorkPolicy.REPLACE,
                req
        );
    }

    /**
     * Baut aus den gespeicherten FolderItems das CSV für den Worker.
     */
    private String buildFolderCsv() {
        Set<FolderItem> items = folderManager.getFolderItems();
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (FolderItem item : items) {
            if (sb.length() > 0) sb.append(",");
            sb.append(item.getUri());
        }
        return sb.toString();
    }
}
