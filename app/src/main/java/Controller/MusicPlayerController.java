package Controller;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.example.myapplication.music.data.Track;

import java.io.IOException;

public class MusicPlayerController {
    private static final String TAG = "MusicPlayerController";

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateProgressRunnable;
    private MusicPlayerListener listener;

    public interface MusicPlayerListener {
        void onTrackStarted(String title, int duration);
        void onProgressUpdated(int progress);
        void onTrackCompleted();
    }

    public MusicPlayerController(MusicPlayerListener listener) {
        this.listener = listener;
        initializeMediaPlayer();
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            stopProgressUpdater();
            if (listener != null) {
                listener.onTrackCompleted();
            }
        });
    }

    public void playTrack(Context context, Track track) {
        if (track == null || track.getUri() == null) {
            Log.e(TAG, "Invalid track or URI");
            return;
        }
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            Uri trackUri = Uri.parse(track.getUri());
            mediaPlayer.setDataSource(context, trackUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (listener != null) {
                String title = track.getTitle() != null ? track.getTitle() : "Unbekannt";
                listener.onTrackStarted(title, mediaPlayer.getDuration());
            }
            startProgressUpdater();
        } catch (IOException e) {
            Log.e(TAG, "Error playing track: " + track.getTitle(), e);
        }
    }

    private void startProgressUpdater() {
        stopProgressUpdater();
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    if(listener != null) {
                        listener.onProgressUpdated(currentPosition);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateProgressRunnable);
    }

    private void stopProgressUpdater() {
        if (updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            startProgressUpdater();
        }
    }

    public void release() {
        stopProgressUpdater();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
