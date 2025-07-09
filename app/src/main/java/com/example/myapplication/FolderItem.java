package com.example.myapplication;

import java.util.Objects;

/**
 * Die FolderItem-Klasse repräsentiert einen Musikordner mithilfe seines Namens und der zugehörigen URI.
 * Dieses reine Model-Objekt ist unveränderlich und gehört damit zum Model im MVC-Muster.
 */
public class FolderItem {
    private final String name;
    private final String uri;

    /**
     * Erzeugt ein neues FolderItem.
     *
     * @param name Der Anzeigename des Ordners.
     * @param uri  Die URI des Ordners.
     */
    public FolderItem(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    /**
     * Gibt den Namen des Ordners zurück.
     *
     * @return Der Ordnername.
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt die URI des Ordners zurück.
     *
     * @return Die Ordner-URI.
     */
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "FolderItem{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderItem)) return false;
        FolderItem that = (FolderItem) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uri);
    }
}
