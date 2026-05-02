package com.sennagi.vibemus.music

import android.content.Context
import android.content.SharedPreferences

class MusicUserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vibemus_user_prefs", Context.MODE_PRIVATE)

    fun getFavoriteIds(): Set<Long> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    fun toggleFavorite(songId: Long): Boolean {
        val current = getFavoriteIds().toMutableSet()
        val added = if (songId in current) {
            current.remove(songId)
            false
        } else {
            current.add(songId)
            true
        }
        prefs.edit()
            .putStringSet(KEY_FAVORITES, current.map { it.toString() }.toSet())
            .apply()
        return added
    }

    fun getRecentPlayIds(): List<Long> {
        return prefs.getString(KEY_RECENTS, "")
            .orEmpty()
            .split(',')
            .mapNotNull { it.toLongOrNull() }
    }

    fun pushRecentPlay(songId: Long) {
        val updated = buildList {
            add(songId)
            addAll(getRecentPlayIds().filter { it != songId })
        }.take(MAX_RECENT_SIZE)

        prefs.edit()
            .putString(KEY_RECENTS, updated.joinToString(","))
            .apply()
    }

    fun getLastPlayedId(): Long? {
        val value = prefs.getLong(KEY_LAST_PLAYED_ID, -1L)
        return value.takeIf { it >= 0L }
    }

    fun saveLastPlayedId(songId: Long) {
        prefs.edit()
            .putLong(KEY_LAST_PLAYED_ID, songId)
            .apply()
    }

    private companion object {
        const val KEY_FAVORITES = "favorite_song_ids"
        const val KEY_RECENTS = "recent_play_ids"
        const val KEY_LAST_PLAYED_ID = "last_played_id"
        const val MAX_RECENT_SIZE = 50
    }
}
