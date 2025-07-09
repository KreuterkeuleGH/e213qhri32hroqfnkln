package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Der FolderAdapter erweitert den ArrayAdapter, um FolderItem-Objekte in einer ListView anzuzeigen.
 * Er implementiert Callbacks für das Löschen eines Ordners sowie einen Long-Click auf einen Eintrag.
 */
public class FolderAdapter extends ArrayAdapter<FolderItem> {

    private final OnFolderDeleteListener deleteListener;
    private final OnFolderLongClickListener longClickListener;

    /**
     * Konstruktor.
     *
     * @param context           Der Kontext.
     * @param items             Die Liste der FolderItem-Objekte.
     * @param deleteListener    Callback, der beim Drücken des Lösch-Buttons ausgelöst wird.
     * @param longClickListener Callback, der bei langem Drücken des Eintrags (außer Lösch-Button) ausgelöst wird.
     */
    public FolderAdapter(Context context, List<FolderItem> items,
                         OnFolderDeleteListener deleteListener,
                         OnFolderLongClickListener longClickListener) {
        super(context, 0, items);
        this.deleteListener = deleteListener;
        this.longClickListener = longClickListener;
    }

    /**
     * Eine statische ViewHolder-Klasse, um die Referenzen der Views zu speichern.
     */
    static class ViewHolder {
        TextView tvFolderName;
        Button btnDeleteFolder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FolderItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.folder_list_item, parent, false);
            holder = new ViewHolder();
            holder.tvFolderName = convertView.findViewById(R.id.tvFolderName);
            holder.btnDeleteFolder = convertView.findViewById(R.id.btnDeleteFolder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Ordnernamen binden.
        holder.tvFolderName.setText(item.getName());

        // Lösch-Button: Klick-Callback für das Entfernen des Ordners.
        holder.btnDeleteFolder.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
        // Verhindern, dass ein langer Klick auf den Button auch den LongClick auslöst.
        holder.btnDeleteFolder.setOnLongClickListener(v -> true);

        // Setze den LongClickListener für die übrige Zeile (ohne den Button).
        convertView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(item);
            }
            return true;
        });

        return convertView;
    }

    /**
     * Callback-Interface für den Löschvorgang eines Ordners.
     */
    public interface OnFolderDeleteListener {
        void onDelete(FolderItem folder);
    }

    /**
     * Callback-Interface für einen Long-Click auf einen Ordner.
     */
    public interface OnFolderLongClickListener {
        void onLongClick(FolderItem folder);
    }
}
