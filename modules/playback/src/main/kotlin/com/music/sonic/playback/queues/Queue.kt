package com.music.sonic.playback.queues

import androidx.media3.common.MediaItem
import com.music.sonic.extensions.metadata
import com.music.sonic.models.MediaMetadata

interface Queue {
  val preloadItem: MediaMetadata?

  suspend fun getInitialStatus(): Status

  fun hasNextPage(): Boolean

  suspend fun nextPage(): List<MediaItem>

  data class Status(
    val title: String?,
    val items: List<MediaItem>,
    val mediaItemIndex: Int,
    val position: Long = 0L,
  ) {
    fun filterExplicit(enabled: Boolean = true) =
      if (enabled) {
        withFilteredItems(items.filterExplicit())
      } else {
        this
      }

    fun filterVideoSongs(disableVideos: Boolean = false) =
      if (disableVideos) {
        withFilteredItems(items.filterVideoSongs(true))
      } else {
        this
      }

    /**
     * Re-points [mediaItemIndex] at the item it originally selected (matched by mediaId) after
     * [newItems] has had entries removed, instead of leaving it as a raw position into a
     * now-shorter list — which would silently select and play a different song than the one the
     * user tapped.
     *
     * Matches by occurrence rather than the first same-mediaId hit, so a queue with the same song
     * listed more than once still re-points at the exact occurrence that was selected instead of
     * always snapping to the first one.
     */
    private fun withFilteredItems(newItems: List<MediaItem>): Status {
      if (newItems.isEmpty()) return copy(items = emptyList(), mediaItemIndex = 0)
      val currentItem = items.getOrNull(mediaItemIndex)
      val newIndex =
        if (currentItem != null) {
          val occurrence =
            items.take(mediaItemIndex + 1).count { it.mediaId == currentItem.mediaId } - 1
          val matchedIndex = newItems
            .withIndex()
            .filter { (_, item) -> item.mediaId == currentItem.mediaId }
            .getOrNull(occurrence)
            ?.index

          if (matchedIndex != null) {
            matchedIndex
          } else {
            val itemsBefore = items.take(mediaItemIndex).map { it.mediaId }.toSet()
            val lastPrecedingMatch = newItems.indexOfLast { it.mediaId in itemsBefore }
            if (lastPrecedingMatch >= 0) lastPrecedingMatch else 0
          }
        } else {
          0
        }
      return copy(
        items = newItems,
        mediaItemIndex = newIndex.coerceIn(0, (newItems.size - 1).coerceAtLeast(0)),
      )
    }
  }
}

fun List<MediaItem>.filterExplicit(enabled: Boolean = true) =
  if (enabled) {
    filterNot { it.metadata?.explicit == true }
  } else {
    this
  }

fun List<MediaItem>.filterVideoSongs(disableVideos: Boolean = false) =
  if (disableVideos) {
    filterNot { it.metadata?.isVideoSong == true }
  } else {
    this
  }
