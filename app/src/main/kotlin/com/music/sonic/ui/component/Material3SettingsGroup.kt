package com.music.sonic.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.music.sonic.ui.utils.scrollToOnHighlight

fun getSettingsSegmentedShape(index: Int, count: Int): Shape {
  val outer = 20.dp
  val inner = 4.dp
  return when {
    count <= 1 -> RoundedCornerShape(outer)
    index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
    index == count - 1 -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
    else -> RoundedCornerShape(inner)
  }
}

@Composable
fun SettingsHeader(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = modifier.padding(top = 8.dp, bottom = 6.dp, start = 4.dp)
  )
}

@Composable
fun Material3SettingsGroup(
  modifier: Modifier = Modifier,
  title: String? = null,
  compact: Boolean = false,
  scrollState: ScrollState? = null,
  items: List<Material3SettingsItem>
) {
  if (items.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = if (compact) 0.dp else 2.dp)
  ) {
    title?.let {
      SettingsHeader(
        text = it,
        modifier = Modifier.padding(bottom = if (compact) 2.dp else 4.dp)
      )
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .then(
                if (item.isHighlighted) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                else Modifier
              )
          ) {
            Material3SettingsItemRow(item = item, compact = compact, scrollState = scrollState)
          }
        }
      }
    }
  }
}

@Composable
private fun Material3SettingsItemRow(
  item: Material3SettingsItem,
  compact: Boolean = false,
  scrollState: ScrollState? = null
) {
  val verticalPadding = if (compact) 6.dp else 8.dp
  val horizontalPadding = if (compact) 12.dp else 14.dp
  val iconContainerSize = if (compact) 32.dp else 36.dp
  val iconSize = if (compact) 18.dp else 20.dp

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(
          enabled = item.enabled && item.onClick != null,
          onClick = { item.onClick?.invoke() }
        )
        .then(
          if (scrollState != null) Modifier.scrollToOnHighlight(scrollState, item.isHighlighted)
          else Modifier
        )
        .padding(
          horizontal = horizontalPadding,
          vertical = verticalPadding
        ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (item.customIcon != null) {
      Box(
        modifier =
          Modifier.size(iconContainerSize)
            .clip(item.iconShape ?: CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
      ) {
        item.customIcon.invoke()
      }
      Spacer(modifier = Modifier.width(12.dp))
    } else
      item.icon?.let { icon ->
        Box(
          modifier =
            Modifier.size(iconContainerSize)
              .clip(item.iconShape ?: CircleShape)
              .background(
                if (item.tintIcon) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else Color.Transparent
              ),
          contentAlignment = Alignment.Center
        ) {
          if (item.showBadge) {
            BadgedBox(badge = { Badge(containerColor = MaterialTheme.colorScheme.error) }) {
              if (item.tintIcon) {
                Icon(
                  painter = icon,
                  contentDescription = null,
                  tint =
                    if (!item.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    else if (item.isHighlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(iconSize)
                )
              } else {
                Image(
                  painter = icon,
                  contentDescription = null,
                  modifier = Modifier.size(iconContainerSize),
                  contentScale = ContentScale.Crop
                )
              }
            }
          } else {
            if (item.tintIcon) {
              Icon(
                painter = icon,
                contentDescription = null,
                tint =
                  if (!item.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                  else if (item.isHighlighted) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize)
              )
            } else {
              Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(iconContainerSize),
                contentScale = ContentScale.Crop
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(12.dp))
      }

    Column(modifier = Modifier.weight(1f)) {
      ProvideTextStyle(
        MaterialTheme.typography.titleMedium.copy(
          fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
          color =
            if (!item.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            else MaterialTheme.colorScheme.onSurface
        )
      ) {
        item.title()
      }

      item.description?.let { desc ->
        Spacer(modifier = Modifier.height(2.dp))
        ProvideTextStyle(
          MaterialTheme.typography.bodyMedium.copy(
            color =
              if (!item.enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
              else MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          desc()
        }
      }
    }

    item.trailingContent?.let { trailing ->
      Spacer(modifier = Modifier.width(10.dp))
      trailing()
    }
  }
}

data class Material3SettingsItem(
  val icon: Painter? = null,
  val customIcon: (@Composable () -> Unit)? = null,
  val title: @Composable () -> Unit,
  val description: (@Composable () -> Unit)? = null,
  val trailingContent: (@Composable () -> Unit)? = null,
  val showBadge: Boolean = false,
  val isHighlighted: Boolean = false,
  val tintIcon: Boolean = true,
  val iconShape: Shape? = null,
  val enabled: Boolean = true,
  val onClick: (() -> Unit)? = null
)
