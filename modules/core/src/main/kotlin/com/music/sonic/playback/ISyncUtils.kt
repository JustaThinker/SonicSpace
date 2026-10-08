package com.music.sonic.playback

import com.music.sonic.db.entities.SongEntity

interface ISyncUtils {
  fun likeSong(song: SongEntity)
}
