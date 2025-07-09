package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemTrackBinding;

/**
 * TrackAdapter ist für die Darstellung einer Liste von {@link Track} Objekten in einer RecyclerView zuständig.
 * Nutzt ListAdapter + DiffUtil für performante, animierte Listenupdates und ViewBinding für Typsicherheit.
 * Setzt auf stabile IDs und bietet einen Click-Callback.
 */
public class TrackAdapter
        extends ListAdapter<Track, TrackAdapter.TrackViewHolder> {

    private final OnTrackClickListener clickListener;

    /**
     * Callback-Interface für Item-Clicks.
     */
    public interface OnTrackClickListener {
        void onTrackClick(@NonNull Track track);
    }

    public TrackAdapter(@NonNull OnTrackClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        // Aktiviert stabile IDs für bessere Animationen
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<Track> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Track>() {
                @Override
                public boolean areItemsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
                    String oldTitle = oldItem.getTitle() != null ? oldItem.getTitle() : "";
                    String newTitle = newItem.getTitle() != null ? newItem.getTitle() : "";
                    return oldTitle.equals(newTitle);
                }
            };

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrackBinding binding = ItemTrackBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TrackViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = getItem(position);
        holder.bind(track, clickListener);
    }

    @Override
    public long getItemId(int position) {
        // Gibt die Track-ID als stabile ID zurück
        return getItem(position).getId();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrackBinding binding;

        TrackViewHolder(@NonNull ItemTrackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull Track track, @NonNull OnTrackClickListener listener) {
            String title = track.getTitle();
            binding.trackTitle.setText(
                    (title != null && !title.isEmpty()) ? title : "Unbekannt"
            );
            // Klick-Listener registrieren
            binding.getRoot().setOnClickListener(v -> listener.onTrackClick(track));
        }
    }
}