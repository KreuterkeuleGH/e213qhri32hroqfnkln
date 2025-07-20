package com.example.myapplication.music.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.music.data.MusicRepository;

public class CleanupWorker extends Worker {

    private static final String TAG = "CleanupWorker";

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            MusicRepository.getInstance(getApplicationContext()).cleanupDeletedTracks();
            Log.d(TAG, "CleanupWorker erfolgreich durchgeführt.");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Fehler im CleanupWorker", e);
            return Result.failure();
        }
    }
}
