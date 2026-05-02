package com.sennagi.vibemus.music

import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore

data class SongItem(
    val id: Long,
    val title: String,
    val artist: String,
    val folder: String,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val durationMs: Long,
    val dateAddedSec: Long,
    val formatLabel: String
)

data class FolderChoice(
    val path: String,
    val songCount: Int,
    val selected: Boolean = true
)

class MusicFolderPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vibemus_music_library", Context.MODE_PRIVATE)

    fun getSelectedFolders(): Set<String> {
        return prefs.getStringSet(KEY_SELECTED_FOLDERS, emptySet()).orEmpty()
    }

    fun saveSelectedFolders(folders: Set<String>) {
        prefs.edit().putStringSet(KEY_SELECTED_FOLDERS, folders).apply()
    }

    fun clearSelectedFolders() {
        prefs.edit().remove(KEY_SELECTED_FOLDERS).apply()
    }

    private companion object {
        const val KEY_SELECTED_FOLDERS = "selected_folders"
    }
}

object MusicLibrary {
    private val albumArtBaseUri: Uri = Uri.parse("content://media/external/audio/albumart")

    fun scanDevice(context: Context): List<SongItem> {
        val contentResolver = context.contentResolver
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.SIZE} > 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        return buildList {
            contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val folderIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex).orEmpty().ifBlank { "\u672a\u77e5\u6b4c\u66f2" }
                    val artist = cursor.getString(artistIndex).orEmpty().ifBlank { "\u672a\u77e5\u827a\u672f\u5bb6" }
                    val albumId = cursor.getLong(albumIdIndex)
                    val durationMs = cursor.getLong(durationIndex)
                    val dateAddedSec = cursor.getLong(dateAddedIndex)
                    val folderPath = cursor.getString(folderIndex)
                        .orEmpty()
                        .trim()
                        .trimEnd('/')
                        .ifBlank { "\u6839\u76ee\u5f55\u97f3\u9891" }
                    val displayName = cursor.getString(displayNameIndex).orEmpty()
                    val mimeType = cursor.getString(mimeTypeIndex).orEmpty()

                    add(
                        SongItem(
                            id = id,
                            title = title,
                            artist = artist,
                            folder = folderPath,
                            contentUri = ContentUris.withAppendedId(collection, id),
                            albumArtUri = albumId.takeIf { it > 0L }?.let {
                                ContentUris.withAppendedId(albumArtBaseUri, it)
                            },
                            durationMs = durationMs,
                            dateAddedSec = dateAddedSec,
                            formatLabel = resolveFormatLabel(displayName = displayName, mimeType = mimeType)
                        )
                    )
                }
            }
        }
    }

    fun buildFolderChoices(
        songs: List<SongItem>,
        savedFolders: Set<String> = emptySet()
    ): List<FolderChoice> {
        val useSavedSelection = savedFolders.isNotEmpty()
        return songs
            .groupBy { it.folder }
            .map { (folder, tracks) ->
                FolderChoice(
                    path = folder,
                    songCount = tracks.size,
                    selected = if (useSavedSelection) folder in savedFolders else true
                )
            }
            .sortedBy { it.path.lowercase() }
    }

    fun filterSongsByFolders(
        songs: List<SongItem>,
        selectedFolders: Set<String>
    ): List<SongItem> {
        if (selectedFolders.isEmpty()) return emptyList()
        return songs.filter { it.folder in selectedFolders }
    }

    private fun resolveFormatLabel(displayName: String, mimeType: String): String {
        val extension = displayName.substringAfterLast('.', "").uppercase()
        if (extension.isNotBlank()) return extension

        return mimeType.substringAfterLast('/').uppercase().ifBlank { "AUDIO" }
    }
}
