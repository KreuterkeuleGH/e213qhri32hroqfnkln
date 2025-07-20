package com.example.myapplication.music.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

import com.example.myapplication.app.MyApplication;
import com.example.myapplication.music.data.MusicRepository;
import com.example.myapplication.music.data.Track;

import java.util.List;

import javax.inject.Inject;

public class MusicLoaderWorker extends Worker {

    @Inject
    MusicRepository repository;

    public MusicLoaderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
        MyApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        int pageSize    = getInputData().getInt("page_size", 10);
        String folderCsv = getInputData().getString("folder_uris");

        // Wenn kein Filter angegeben, übergeben wir null
        String filter = (folderCsv != null && !folderCsv.isEmpty())
                ? folderCsv
                : null;

        int page = 0;
        List<Track> tracks;

        do {
            // Holt Seite page mit pageSize und optionalem Filter
            tracks = repository.getCachedTracksPage(page, pageSize, filter);

            // Du könntest hier z.B. eine Notification oder Log-Einträge hinzufügen:
            // Log.d(TAG, "Seite " + page + ": " + tracks.size() + " Tracks geladen");

            page++;
        } while (tracks != null && !tracks.isEmpty());

        return Result.success();
    }

    public static class Factory extends WorkerFactory {
        @Override
        public ListenableWorker createWorker(
                @NonNull Context context,
                @NonNull String workerClassName,
                @NonNull WorkerParameters params
        ) {
            return new MusicLoaderWorker(context, params);
        }
    }
}
