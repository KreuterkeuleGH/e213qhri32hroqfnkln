package com.example.myapplication;

/**
 * Schnittstelle für den Callback, der ausgelöst wird, wenn ein Ordner gelöscht werden soll.
 * Diese Callback-Schnittstelle wird z. B. in Adaptern verwendet, um Löschaktionen an die
 * zuständige Komponente weiterzureichen.
 */
@FunctionalInterface
public interface OnFolderDeleteListener {
    /**
     * Wird aufgerufen, wenn ein Ordner gelöscht werden soll.
     *
     * @param folder Das FolderItem, das gelöscht werden soll.
     */
    void onDelete(FolderItem folder);
}
