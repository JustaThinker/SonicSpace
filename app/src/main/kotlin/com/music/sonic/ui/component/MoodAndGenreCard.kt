package com.music.sonic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.innertube.pages.MoodAndGenres

val MOOD_SPACING = 12.dp
val MOOD_CARD_SHAPE = RoundedCornerShape(18.dp)
const val MOOD_CARD_ASPECT = 1.72f
const val MOOD_STRIPE_FRACTION = 0.28f
val MOOD_MIN_CARD_WIDTH = 220.dp
const val MOOD_MAX_COLUMNS = 6
val PAGE_GUTTER = 16.dp

fun moodColumns(available: Dp): Int {
  val row = available - PAGE_GUTTER * 2
  return ((row + MOOD_SPACING) / (MOOD_MIN_CARD_WIDTH + MOOD_SPACING))
    .toInt()
    .coerceIn(2, MOOD_MAX_COLUMNS)
}

val MoodAndGenresButtonHeight = 64.dp

@Composable
fun MoodAndGenreCard(
  item: MoodAndGenres.Item,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  thumbnailUrl: String? = item.thumbnailUrl,
) {
  MoodAndGenreCard(
    title = item.title,
    stripeColor = item.stripeColor,
    thumbnailUrl = thumbnailUrl ?: item.thumbnailUrl,
    onClick = onClick,
    modifier = modifier,
  )
}

@Composable
fun MoodAndGenreCard(
  title: String,
  stripeColor: Long?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  thumbnailUrl: String? = null,
) {
  val tone = remember(stripeColor, title) { moodTone(title, stripeColor) }

  Box(
    modifier =
      modifier
        .aspectRatio(MOOD_CARD_ASPECT)
        .clip(MOOD_CARD_SHAPE)
        .background(tone.stripe)
        .clickable(onClick = onClick)
  ) {
    Box(
      modifier =
        Modifier.align(Alignment.CenterEnd)
          .fillMaxHeight()
          .fillMaxWidth(1f - MOOD_STRIPE_FRACTION)
          .background(tone.placeholder)
    ) {
      if (!thumbnailUrl.isNullOrBlank()) {
        AsyncImage(
          model = thumbnailUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          colorFilter = tone.duotone,
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            Brush.verticalGradient(
              0f to Color.Black.copy(alpha = 0.24f),
              0.62f to Color.Transparent
            )
          )
    )

    Text(
      text = title,
      style =
        MaterialTheme.typography.titleMedium.copy(
          shadow =
            Shadow(
              color = Color.Black.copy(alpha = 0.35f),
              offset = Offset(0f, 1f),
              blurRadius = 6f
            )
        ),
      fontWeight = FontWeight.Bold,
      color = Color.White,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 14.dp, vertical = 12.dp)
    )
  }
}

private class MoodTone(
  val stripe: Color,
  val placeholder: Color,
  val duotone: ColorFilter,
)

private fun moodTone(title: String, stripeColor: Long?): MoodTone {
  val base = stripeColor?.let { Color(it.toInt()) } ?: fallbackMoodColor(title)
  val stripe = lerp(base, Color.Black, 0.08f)
  val shadow = lerp(base, Color.Black, 0.68f)
  val highlight = lerp(base, Color.White, 0.32f)
  return MoodTone(
    stripe = stripe,
    placeholder = lerp(shadow, highlight, 0.45f),
    duotone = duotone(shadow, highlight),
  )
}

private fun duotone(shadow: Color, highlight: Color): ColorFilter {
  fun channel(from: Float, to: Float): FloatArray {
    val span = to - from
    return floatArrayOf(span * 0.299f, span * 0.587f, span * 0.114f, 0f, from * 255f)
  }
  return ColorFilter.colorMatrix(
    ColorMatrix(
      channel(shadow.red, highlight.red) +
        channel(shadow.green, highlight.green) +
        channel(shadow.blue, highlight.blue) +
        floatArrayOf(0f, 0f, 0f, 1f, 0f)
    )
  )
}

private fun fallbackMoodColor(title: String): Color =
  when ((title.hashCode() and Int.MAX_VALUE) % 8) {
    0 -> Color(0xFFCC6A55)
    1 -> Color(0xFFC07A92)
    2 -> Color(0xFF9C8AC0)
    3 -> Color(0xFF8090C8)
    4 -> Color(0xFFD0A060)
    5 -> Color(0xFF6A88B0)
    6 -> Color(0xFF7AAED0)
    else -> Color(0xFF86B890)
  }
