package com.music.sonic.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.LruCache
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaWidgetArt {
  private val circleCache = LruCache<String, Bitmap>(20)
  private val pillCache = LruCache<String, Bitmap>(30)

  fun peekCircle(key: String, discPx: Int): Bitmap? {
    return circleCache.get("$key-$discPx")
  }

  fun peekPill(key: String, widthPx: Int, heightPx: Int): Bitmap? {
    return pillCache.get("$key-$widthPx-$heightPx")
  }

  suspend fun circle(
    context: Context,
    url: String?,
    discPx: Int,
    key: String
  ): Bitmap? {
    peekCircle(key, discPx)?.let { return it }
    val source = loadBitmap(context, url, discPx) ?: return null
    val circular = createCircularBitmap(source, discPx)
    circleCache.put("$key-$discPx", circular)
    return circular
  }

  suspend fun pill(
    context: Context,
    url: String?,
    widthPx: Int,
    heightPx: Int,
    key: String?
  ): Bitmap? {
    if (key != null) {
      peekPill(key, widthPx, heightPx)?.let { return it }
    }
    val source = loadBitmap(context, url, 200) ?: return null
    val pill = createPillBitmap(source, widthPx, heightPx)
    if (key != null) {
      pillCache.put("$key-$widthPx-$heightPx", pill)
    }
    return pill
  }

  private suspend fun loadBitmap(context: Context, url: String?, sizePx: Int): Bitmap? =
    withContext(Dispatchers.IO) {
      if (url.isNullOrBlank()) return@withContext null
      try {
        val request = ImageRequest.Builder(context)
          .data(url)
          .size(sizePx, sizePx)
          .allowHardware(false)
          .crossfade(false)
          .build()
        context.imageLoader.execute(request).image?.toBitmap()
      } catch (e: Exception) {
        null
      }
    }

  private fun createCircularBitmap(source: Bitmap, sizePx: Int): Bitmap {
    val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
      val matrix = Matrix()
      val scale = sizePx.toFloat() / source.width.coerceAtMost(source.height)
      matrix.setScale(scale, scale)
      shader.setLocalMatrix(matrix)
      this.shader = shader
    }
    val radius = sizePx / 2f
    canvas.drawCircle(radius, radius, radius, paint)
    return output
  }

  private fun createPillBitmap(source: Bitmap, widthPx: Int, heightPx: Int): Bitmap {
    val output = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val blurredSmall = blurSmall(source)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
      val shader = BitmapShader(blurredSmall, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
      val matrix = Matrix()
      val scaleX = widthPx.toFloat() / blurredSmall.width
      val scaleY = heightPx.toFloat() / blurredSmall.height
      matrix.setScale(scaleX, scaleY)
      shader.setLocalMatrix(matrix)
      this.shader = shader
    }

    val rx = heightPx / 2f
    val ry = heightPx / 2f
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), rx, ry, paint)

    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(80, 0, 0, 0)
    }
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), rx, ry, overlayPaint)

    return output
  }

  private fun blurSmall(source: Bitmap): Bitmap {
    val width = 12
    val height = 12
    val small = Bitmap.createScaledBitmap(source, width, height, true)
    val pixels = IntArray(width * height)
    small.getPixels(pixels, 0, width, 0, 0, width, height)
    val outputPixels = IntArray(width * height)

    for (y in 0 until height) {
      for (x in 0 until width) {
        var r = 0; var g = 0; var b = 0; var count = 0
        for (dy in -2..2) {
          for (dx in -2..2) {
            val nx = (x + dx).coerceIn(0, width - 1)
            val ny = (y + dy).coerceIn(0, height - 1)
            val p = pixels[ny * width + nx]
            r += (p shr 16) and 0xFF
            g += (p shr 8) and 0xFF
            b += p and 0xFF
            count++
          }
        }
        val avgR = r / count
        val avgG = g / count
        val avgB = b / count
        outputPixels[y * width + x] = (0xFF shl 24) or (avgR shl 16) or (avgG shl 8) or avgB
      }
    }
    val blurred = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    blurred.setPixels(outputPixels, 0, width, 0, 0, width, height)
    return blurred
  }
}
