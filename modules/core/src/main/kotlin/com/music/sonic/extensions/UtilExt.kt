package com.music.sonic.extensions

fun <T> tryOrNull(block: () -> T): T? =
  try {
    block()
  } catch (e: Exception) {
    null
  }
