package com.music.sonic.widget

import android.content.Context

data class MediaWidgetSnapshot(
  val artworkUrl: String? = null,
  val hasTrack: Boolean = false,
  val title: String = "",
  val artist: String = "",
  val isPlaying: Boolean = false,
  val isLiked: Boolean = false,
  val hasPrevious: Boolean = true,
  val hasNext: Boolean = true,
  val shuffleEnabled: Boolean = false,
) {
  fun save(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putString(KEY_ARTWORK_URL, artworkUrl)
      .putBoolean(KEY_HAS_TRACK, hasTrack)
      .putString(KEY_TITLE, title)
      .putString(KEY_ARTIST, artist)
      .putBoolean(KEY_IS_PLAYING, isPlaying)
      .putBoolean(KEY_IS_LIKED, isLiked)
      .putBoolean(KEY_HAS_PREVIOUS, hasPrevious)
      .putBoolean(KEY_HAS_NEXT, hasNext)
      .putBoolean(KEY_SHUFFLE_ENABLED, shuffleEnabled)
      .apply()
  }

  companion object {
    private const val PREFS_NAME = "media_widget_snapshot_prefs"
    private const val KEY_ARTWORK_URL = "artwork_url"
    private const val KEY_HAS_TRACK = "has_track"
    private const val KEY_TITLE = "title"
    private const val KEY_ARTIST = "artist"
    private const val KEY_IS_PLAYING = "is_playing"
    private const val KEY_IS_LIKED = "is_liked"
    private const val KEY_HAS_PREVIOUS = "has_previous"
    private const val KEY_HAS_NEXT = "has_next"
    private const val KEY_SHUFFLE_ENABLED = "shuffle_enabled"

    fun load(context: Context): MediaWidgetSnapshot {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      return MediaWidgetSnapshot(
        artworkUrl = prefs.getString(KEY_ARTWORK_URL, null),
        hasTrack = prefs.getBoolean(KEY_HAS_TRACK, false),
        title = prefs.getString(KEY_TITLE, "") ?: "",
        artist = prefs.getString(KEY_ARTIST, "") ?: "",
        isPlaying = prefs.getBoolean(KEY_IS_PLAYING, false),
        isLiked = prefs.getBoolean(KEY_IS_LIKED, false),
        hasPrevious = prefs.getBoolean(KEY_HAS_PREVIOUS, true),
        hasNext = prefs.getBoolean(KEY_HAS_NEXT, true),
        shuffleEnabled = prefs.getBoolean(KEY_SHUFFLE_ENABLED, false),
      )
    }
  }
}
