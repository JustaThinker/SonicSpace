package com.music.sonic.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.music.sonic.ui.theme.extractGradientColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AlbumGradient(
  thumbnailUrl: String?,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val surfaceColor = MaterialTheme.colorScheme.surface

  var extractedColors by remember { mutableStateOf<List<Color>>(emptyList()) }

  LaunchedEffect(thumbnailUrl) {
    if (thumbnailUrl != null) {
      withContext(Dispatchers.IO) {
        try {
          val request =
            ImageRequest.Builder(context)
              .data(thumbnailUrl)
              .size(100, 100)
              .allowHardware(false)
              .build()
          val result = context.imageLoader.execute(request)
          val bitmap = result.image?.toBitmap()

          if (bitmap != null) {
            val colors = withContext(Dispatchers.Default) {
              bitmap.extractGradientColors()
            }
            extractedColors = colors
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  val color1 by
    animateColorAsState(
      targetValue = extractedColors.getOrNull(0)?.copy(alpha = 0.5f) ?: surfaceColor,
      animationSpec = tween(durationMillis = 800),
      label = "AlbumGradientColor1"
    )
  val color2 by
    animateColorAsState(
      targetValue = extractedColors.getOrNull(1)?.copy(alpha = 0.3f) ?: surfaceColor,
      animationSpec = tween(durationMillis = 800),
      label = "AlbumGradientColor2"
    )

  Box(
    modifier =
      modifier.background(
        Brush.verticalGradient(
          colorStops = arrayOf(0.0f to color1, 0.5f to color2, 1.0f to surfaceColor)
        )
      )
  )
}
