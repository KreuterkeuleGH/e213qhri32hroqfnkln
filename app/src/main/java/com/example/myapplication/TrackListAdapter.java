package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.List;

/**
 * TrackListAdapter ist verantwortlich für die Darstellung einer Liste von {@link Track} Objekten in einer ListView.
 * Diese Klasse gehört rein zum View-Teil des MVC-Musters, da sie keinerlei Geschäftslogik enthält, sondern
 * ausschließlich für die Darstellung der Track-Daten zuständig ist.
 */
public class TrackListAdapter extends ArrayAdapter<Track> {

    private final Context context;
    private final List<Track> trackList;

    public TrackListAdapter(@NonNull Context context, List<Track> trackList) {
        super(context, R.layout.item_track, trackList);
        this.context = context;
        this.trackList = trackList;
    }

    /**
     * ViewHolder-Muster um unnötige Aufrufe von findViewById zu vermeiden.
     */
    static class ViewHolder {
        TextView trackTitle;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Neuinflate und ViewHolder erstellen
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_track, parent, false);

            holder = new ViewHolder();
            holder.trackTitle = convertView.findViewById(R.id.trackTitle);
            convertView.setTag(holder);
        } else {
            // Bestehenden ViewHolder wiederverwenden
            holder = (ViewHolder) convertView.getTag();
        }

        // Hole das aktuelle Track-Objekt und setze den Titel (Fallback zu "Unbekannt" falls leer)
        Track track = trackList.get(position);
        String title = (track.getTitle() != null && !track.getTitle().isEmpty())
                ? track.getTitle()
                : "Unbekannt";
        holder.trackTitle.setText(title);

        return convertView;
    }
}
