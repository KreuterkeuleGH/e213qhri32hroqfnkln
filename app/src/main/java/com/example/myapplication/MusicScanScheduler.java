package com.example.myapplication;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.MusicLoaderWorker;

import java.util.concurrent.TimeUnit;

@Singleton
public class MusicScanScheduler {
    private static final String TAG_PERIODIC = "PERIODIC_SCAN";
    private static final String TAG_ON_RESUME = "ON_RESUME_SCAN";

    private final WorkManager wm;
    private final int pageSize;

    @Inject
    public MusicScanScheduler(@ApplicationContext Context ctx,
                              @Named("batch_size") int pageSize) {
        this.wm = WorkManager.getInstance(ctx);
        this.pageSize = pageSize;
    }

    public void schedulePeriodic() {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                MusicLoaderWorker.class, 15, TimeUnit.MINUTES)
                .addTag(TAG_PERIODIC)
                .setInputData(new Data.Builder()
                        .putInt("page_size", pageSize)
                        .build())
                .build();

        wm.enqueueUniquePeriodicWork(
                TAG_PERIODIC, ExistingPeriodicWorkPolicy.REPLACE, req);
    }

    public void triggerOneTime(String foldersCsv) {
        Data.Builder data = new Data.Builder()
                .putInt("page_size", pageSize);
        if (foldersCsv != null) data.putString("folder_uris", foldersCsv);

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(MusicLoaderWorker.class)
                .addTag(TAG_ON_RESUME)
                .setInputData(data.build())
                .build();

        wm.enqueueUniqueWork(
                TAG_ON_RESUME, ExistingWorkPolicy.REPLACE, req);
    }
}
