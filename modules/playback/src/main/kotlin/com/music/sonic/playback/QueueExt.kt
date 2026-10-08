package com.music.sonic.playback

import com.music.sonic.extensions.toMediaItem
import com.music.sonic.models.MediaMetadata
import com.music.sonic.models.PersistQueue
import com.music.sonic.models.QueueData
import com.music.sonic.models.QueueType
import com.music.sonic.playback.queues.ListQueue
import com.music.sonic.playback.queues.LocalAlbumRadio
import com.music.sonic.playback.queues.Queue
import com.music.sonic.playback.queues.YouTubeAlbumRadio
import com.music.sonic.playback.queues.YouTubeQueue

fun Queue.toPersistQueue(
  title: String?,
  items: List<MediaMetadata>,
  mediaItemIndex: Int,
  position: Long
): PersistQueue {
  return when (this) {
    is ListQueue ->
      PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.LIST
      )
    is YouTubeQueue -> {

      val endpoint = "youtube_queue"
      PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.YOUTUBE,
        queueData = QueueData.YouTubeData(endpoint = endpoint)
      )
    }
    is YouTubeAlbumRadio -> {

      PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.YOUTUBE_ALBUM_RADIO,
        queueData = QueueData.YouTubeAlbumRadioData(playlistId = "youtube_album_radio")
      )
    }
    is LocalAlbumRadio -> {

      PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.LOCAL_ALBUM_RADIO,
        queueData = QueueData.LocalAlbumRadioData(albumId = "local_album_radio", startIndex = 0)
      )
    }
    else ->
      PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.LIST
      )
  }
}

fun PersistQueue.toQueue(): Queue {
  return when (queueType) {
    is QueueType.LIST ->
      ListQueue(
        title = title,
        items = items.map { it.toMediaItem() },
        startIndex = mediaItemIndex,
        position = position
      )
    is QueueType.YOUTUBE -> {

      ListQueue(
        title = title,
        items = items.map { it.toMediaItem() },
        startIndex = mediaItemIndex,
        position = position
      )
    }
    is QueueType.YOUTUBE_ALBUM_RADIO -> {

      ListQueue(
        title = title,
        items = items.map { it.toMediaItem() },
        startIndex = mediaItemIndex,
        position = position
      )
    }
    is QueueType.LOCAL_ALBUM_RADIO -> {

      ListQueue(
        title = title,
        items = items.map { it.toMediaItem() },
        startIndex = mediaItemIndex,
        position = position
      )
    }
  }
}
