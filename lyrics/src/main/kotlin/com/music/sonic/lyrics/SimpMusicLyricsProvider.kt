package com.music.sonic.lyrics

import android.content.Context
import com.music.simpmusic.SimpMusicLyrics
import com.music.sonic.constants.EnableSimpMusicKey
import com.music.sonic.utils.dataStore
import com.music.sonic.utils.get

object SimpMusicLyricsProvider : LyricsProvider {
  override val name = "SimpMusic"

  override fun isEnabled(context: Context): Boolean = context.dataStore[EnableSimpMusicKey] ?: true

  override suspend fun getLyrics(
    id: String,
    title: String,
    artist: String,
    duration: Int,
    album: String?,
  ): Result<String> = SimpMusicLyrics.getLyrics(id, duration)

  override suspend fun getAllLyrics(
    id: String,
    title: String,
    artist: String,
    duration: Int,
    album: String?,
    callback: (String) -> Unit,
  ) {
    SimpMusicLyrics.getAllLyrics(id, duration, callback)
  }
}
