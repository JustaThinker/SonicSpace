package com.music.sonic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.sonic.R
import com.music.sonic.ui.theme.BitChordAccentRed

enum class ThemeOption(
  val title: String,
  val subtitle: String,
  val description: String,
  val icon: ImageVector
) {
  STATIC(
    title = "Static",
    subtitle = "Non-Dynamic Palette",
    description = "Uses a consistent, non-dynamic color palette. Choose your custom accent color or keep the default.",
    icon = Icons.Rounded.Palette
  ),
  DYNAMIC(
    title = "Dynamic",
    subtitle = "Adaptive Palette",
    description = "Dynamically extracts colors in real-time from your system wallpaper and currently playing album art.",
    icon = Icons.Rounded.AutoAwesome
  ),
  BITCHORD(
    title = "BitChord",
    subtitle = "Signature Duotone",
    description = "Sleek, high-contrast duotone aesthetic palette with signature crimson red accents.",
    icon = Icons.Rounded.GraphicEq
  )
}

fun getActiveThemeOption(
  dynamicTheme: Boolean,
  bitChordPaletteEnabled: Boolean,
  liquidGlassEnabled: Boolean = false
): ThemeOption {
  return when {
    liquidGlassEnabled || bitChordPaletteEnabled -> ThemeOption.BITCHORD
    dynamicTheme -> ThemeOption.DYNAMIC
    else -> ThemeOption.STATIC
  }
}

@Composable
fun ThemeSliderSelector(
  selectedOption: ThemeOption,
  onOptionSelected: (ThemeOption) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  val density = LocalDensity.current
  val optionCount = ThemeOption.entries.size

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxWidth()
        .height(54.dp)
        .clip(RoundedCornerShape(27.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f))
        .border(
          BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
          RoundedCornerShape(27.dp)
        )
        .padding(4.dp)
  ) {
    val totalWidth = maxWidth
    val itemWidth = totalWidth / optionCount
    val itemWidthPx = with(density) { itemWidth.toPx() }
    val maxDragPx = with(density) { (totalWidth - itemWidth).toPx() }

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    val targetSelectedIndex = selectedOption.ordinal
    val animatedThumbOffset by
      animateDpAsState(
        targetValue = itemWidth * targetSelectedIndex,
        animationSpec =
          spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
          ),
        label = "themeSliderThumbOffset"
      )

    val currentOffset =
      if (isDragging) {
        with(density) { dragOffsetPx.toDp() }.coerceIn(0.dp, totalWidth - itemWidth)
      } else {
        animatedThumbOffset
      }

    val thumbBrush =
      when (selectedOption) {
        ThemeOption.BITCHORD ->
          Brush.horizontalGradient(
            colors = listOf(BitChordAccentRed, Color(0xFFD61E37))
          )
        ThemeOption.DYNAMIC ->
          Brush.horizontalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
            )
          )
        ThemeOption.STATIC ->
          Brush.horizontalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
            )
          )
      }

    // Sliding indicator thumb
    Box(
      modifier =
        Modifier
          .offset(x = currentOffset)
          .width(itemWidth)
          .fillMaxHeight()
          .shadow(elevation = 3.dp, shape = RoundedCornerShape(23.dp))
          .clip(RoundedCornerShape(23.dp))
          .background(thumbBrush)
    )

    // Gesture detector & interactive row
    Row(
      modifier =
        Modifier
          .fillMaxSize()
          .pointerInput(enabled, selectedOption) {
            if (!enabled) return@pointerInput
            detectHorizontalDragGestures(
              onDragStart = { offset ->
                isDragging = true
                val initialX = offset.x - (itemWidthPx / 2f)
                dragOffsetPx = initialX.coerceIn(0f, maxDragPx)
              },
              onDragEnd = {
                isDragging = false
                val targetIndex =
                  ((dragOffsetPx + itemWidthPx / 2f) / itemWidthPx)
                    .toInt()
                    .coerceIn(0, optionCount - 1)
                val newOption = ThemeOption.entries[targetIndex]
                if (newOption != selectedOption) {
                  onOptionSelected(newOption)
                }
              },
              onDragCancel = {
                isDragging = false
              },
              onHorizontalDrag = { _, dragAmount ->
                dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(0f, maxDragPx)
              }
            )
          },
      verticalAlignment = Alignment.CenterVertically
    ) {
      ThemeOption.entries.forEach { option ->
        val isSelected = option == selectedOption
        val contentColor by
          animateColorAsState(
            targetValue =
              if (isSelected) Color.White
              else MaterialTheme.colorScheme.onSurfaceVariant,
            label = "themeSliderContentColor"
          )

        Box(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(23.dp))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
              ) {
                if (option != selectedOption) {
                  onOptionSelected(option)
                }
              },
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = option.icon,
              contentDescription = null,
              tint = contentColor,
              modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = option.title,
              style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = contentColor
            )
          }
        }
      }
    }
  }
}

@Composable
fun ThemeStyleCard(
  selectedOption: ThemeOption,
  onOptionSelected: (ThemeOption) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  headerTitle: String = "Theme Style",
  contentBelowSlider: (@Composable () -> Unit)? = null
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
      ),
    elevation = CardDefaults.cardElevation(0.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Box(
          modifier =
            Modifier.size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (selectedOption == ThemeOption.BITCHORD) BitChordAccentRed.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.primaryContainer
              ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = selectedOption.icon,
            contentDescription = null,
            tint =
              if (selectedOption == ThemeOption.BITCHORD) BitChordAccentRed
              else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = headerTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = selectedOption.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color =
              if (selectedOption == ThemeOption.BITCHORD) BitChordAccentRed
              else MaterialTheme.colorScheme.primary
          )
        }
      }

      // The fused slider selector
      ThemeSliderSelector(
        selectedOption = selectedOption,
        onOptionSelected = onOptionSelected,
        enabled = enabled
      )

      // Descriptive banner
      AnimatedContent(
        targetState = selectedOption,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "themeStyleDescription"
      ) { option ->
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f))
              .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (option == ThemeOption.BITCHORD) {
            Box(
              modifier =
                Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(BitChordAccentRed)
            )
          }
          Text(
            text = option.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      contentBelowSlider?.invoke()
    }
  }
}
