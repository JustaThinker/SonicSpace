package com.music.sonic.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object MediaWidgetActions {
  const val ACTION_LIKE = MusicWidgetReceiver.ACTION_LIKE
  const val ACTION_PREVIOUS = MusicWidgetReceiver.ACTION_PREVIOUS
  const val ACTION_TOGGLE = MusicWidgetReceiver.ACTION_PLAY_PAUSE
  const val ACTION_NEXT = MusicWidgetReceiver.ACTION_NEXT
  const val ACTION_SHUFFLE = MusicWidgetReceiver.ACTION_SHUFFLE

  fun pendingIntent(context: Context, action: String): PendingIntent {
    val intent = Intent(context, MusicWidgetReceiver::class.java).apply {
      this.action = action
    }
    val requestCode = when (action) {
      ACTION_LIKE -> 10
      ACTION_PREVIOUS -> 11
      ACTION_TOGGLE -> 12
      ACTION_NEXT -> 13
      ACTION_SHUFFLE -> 14
      else -> 15
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }
}
