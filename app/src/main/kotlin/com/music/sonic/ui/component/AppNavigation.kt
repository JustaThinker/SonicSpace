package com.music.sonic.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.sonic.R
import com.music.sonic.ui.component.LocalGlassEffectConfig
import com.music.sonic.ui.component.liquidGlass
import com.music.sonic.ui.screens.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Immutable private data class NavItemState(val isSelected: Boolean, val iconRes: Int)

@Stable
fun isRouteSelected(
  currentRoute: String?,
  screenRoute: String,
  navigationItems: List<Screens>
): Boolean {
  if (currentRoute == null) return false
  if (currentRoute == screenRoute) return true
  if (screenRoute == Screens.Search.route && (currentRoute == "search" || currentRoute.startsWith("search/") || currentRoute.startsWith("search_input"))) return true
  return navigationItems.any { it.route == screenRoute } && currentRoute.startsWith("$screenRoute/")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationRail(
  navigationItems: List<Screens>,
  currentRoute: String?,
  onItemClick: (Screens, Boolean) -> Unit,
  modifier: Modifier = Modifier,
  pureBlack: Boolean = false,
  onSearchLongClick: (() -> Unit)? = null,
  onMusicRecognitionClick: (() -> Unit)? = null,
  musicRecognitionContentDescription: String = "",
  onShuffleClick: (() -> Unit)? = null,
  shuffleEnabled: Boolean = false,
  shuffleIconRes: Int? = null,
  shuffleContentDescription: String = ""
) {
  val containerColor = if (pureBlack) Color.Black else if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF121212) else MaterialTheme.colorScheme.surface
  val haptics = LocalHapticFeedback.current
  val viewConfiguration = LocalViewConfiguration.current
  var showMoreOptionsSheet by rememberSaveable { mutableStateOf(false) }
  val hasMoreOptions = onShuffleClick != null && shuffleIconRes != null

  NavigationRail(modifier = modifier, containerColor = containerColor) {
    Spacer(modifier = Modifier.weight(1f))

    navigationItems.forEach { screen ->
      val isSelected =
        remember(currentRoute, screen.route) {
          isRouteSelected(currentRoute, screen.route, navigationItems)
        }
      val iconRes =
        remember(isSelected, screen) {
          if (isSelected) screen.iconIdActive else screen.iconIdInactive
        }

      val isSearchItem = screen == Screens.Search && onSearchLongClick != null
      val interactionSource = remember { MutableInteractionSource() }

      if (isSearchItem) {
        LaunchedEffect(interactionSource) {
          var isLongClick = false
          interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
              is PressInteraction.Press -> {
                isLongClick = false
                delay(viewConfiguration.longPressTimeoutMillis)
                isLongClick = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSearchLongClick.invoke()
              }
              is PressInteraction.Release -> {
                if (!isLongClick) {
                  onItemClick(screen, isSelected)
                }
              }
              is PressInteraction.Cancel -> {
                isLongClick = false
              }
            }
          }
        }
      }

      NavigationRailItem(
        selected = isSelected,
        onClick = {
          if (!isSearchItem) {
            onItemClick(screen, isSelected)
          }
        },
        interactionSource = interactionSource,
        icon = {
          Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(screen.titleId)
          )
        }
      )

      if (screen == Screens.Library && onMusicRecognitionClick != null) {
        NavigationRailItem(
          selected = false,
          onClick = onMusicRecognitionClick,
          icon = {
            Icon(
              painter = painterResource(id = R.drawable.mic),
              contentDescription = musicRecognitionContentDescription
            )
          }
        )
      }
    }

    if (hasMoreOptions) {
      NavigationRailItem(
        selected = false,
        onClick = { showMoreOptionsSheet = true },
        icon = {
          Icon(
            painter = painterResource(id = R.drawable.more_horiz),
            contentDescription = stringResource(R.string.more_label)
          )
        }
      )
    }

    Spacer(modifier = Modifier.weight(1f))
  }

  if (hasMoreOptions && showMoreOptionsSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showMoreOptionsSheet = false },
      sheetState = sheetState,
      dragHandle = { BottomSheetDefaults.DragHandle() },
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = stringResource(R.string.more_options),
          style =
            MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            ),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Material3SettingsGroup(
          items =
            buildList<Material3SettingsItem> {
              if (onShuffleClick != null && shuffleIconRes != null) {
                add(
                  Material3SettingsItem(
                    title = { Text(shuffleContentDescription) },
                    icon = painterResource(shuffleIconRes),
                    trailingContent = {
                      Switch(checked = shuffleEnabled, onCheckedChange = { onShuffleClick() })
                    },
                    onClick = onShuffleClick
                  )
                )
              }
            }
        )
      }
    }
  }
}

@Composable
fun AppNavigationBar(
  navigationItems: List<Screens>,
  currentRoute: String?,
  onItemClick: (Screens, Boolean) -> Unit,
  modifier: Modifier = Modifier,
  pureBlack: Boolean = false,
  slimNav: Boolean = false,
  glassEnabled: Boolean = false,
  hideNavLabels: Boolean = false,
  dynamicNavStyle: Boolean = false,
  onSearchLongClick: (() -> Unit)? = null
) {
  val containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
  val haptics = LocalHapticFeedback.current
  val viewConfiguration = LocalViewConfiguration.current

  val navBarHeight by animateDpAsState(
    targetValue = if (dynamicNavStyle) {
      if (hideNavLabels) 64.dp else 76.dp
    } else {
      80.dp
    },
    label = "navBarHeight"
  )
  val navElevation by animateDpAsState(
    targetValue = if (dynamicNavStyle) 8.dp else 3.dp,
    label = "navElevation"
  )

  val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

  Surface(
    modifier = modifier.height(navBarHeight + bottomPadding),
    color = containerColor,
    tonalElevation = navElevation
  ) {
    val density = LocalDensity.current
    val itemWidths = remember { mutableStateMapOf<Int, Dp>() }
    val itemOffsets = remember { mutableStateMapOf<Int, Dp>() }

    val selectedIndex = navigationItems.indexOfFirst { screen ->
      isRouteSelected(currentRoute, screen.route, navigationItems)
    }
    val hasSelection = selectedIndex >= 0
    val safeIndex = if (hasSelection) selectedIndex else 0

    val targetWidth = if (hasSelection) (itemWidths[safeIndex] ?: 0.dp) else 0.dp
    val targetOffset = if (hasSelection) (itemOffsets[safeIndex] ?: 0.dp) else 0.dp

    val navSpring = remember { spring<Float>(dampingRatio = 0.75f, stiffness = 380f) }

    val targetOffsetPx = with(density) { targetOffset.toPx() }
    val animatedOffsetPx by animateFloatAsState(
      targetValue = targetOffsetPx,
      animationSpec = navSpring,
      label = "navPillOffset"
    )

    val targetWidthPx = with(density) { targetWidth.toPx() }
    val animatedWidthPx by animateFloatAsState(
      targetValue = targetWidthPx,
      animationSpec = navSpring,
      label = "navPillWidth"
    )

    val indicatorAlpha by animateFloatAsState(
      targetValue = if (hasSelection && animatedWidthPx > 1f) 1f else 0f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "navPillAlpha"
    )

    val indicatorColor = if (dynamicNavStyle) {
      MaterialTheme.colorScheme.primaryContainer
    } else {
      MaterialTheme.colorScheme.secondaryContainer
    }

    val pillShape = RoundedCornerShape(24.dp)

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = bottomPadding)
        .padding(horizontal = 8.dp, vertical = 6.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (indicatorAlpha > 0f) {
        Box(
          modifier = Modifier
            .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
            .width(with(density) { animatedWidthPx.toDp() })
            .fillMaxHeight()
            .graphicsLayer { alpha = indicatorAlpha }
            .background(indicatorColor, shape = pillShape)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        navigationItems.forEachIndexed { index, screen ->
          val isSelected = index == selectedIndex
          val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive

          val isSearchItem = screen == Screens.Search && onSearchLongClick != null
          val interactionSource = remember { MutableInteractionSource() }

          if (isSearchItem) {
            LaunchedEffect(interactionSource) {
              var isLongClick = false
              interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                  is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSearchLongClick.invoke()
                  }
                  is PressInteraction.Release -> {
                    if (!isLongClick) {
                      onItemClick(screen, isSelected)
                    }
                  }
                  is PressInteraction.Cancel -> {
                    isLongClick = false
                  }
                }
              }
            }
          }

          val animatedScale by animateFloatAsState(
            targetValue = if (dynamicNavStyle && isSelected) 1.22f else 1.0f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMediumLow
            ),
            label = "dynamicNavIconScale"
          )

          val selectedContentColor = if (dynamicNavStyle) {
            MaterialTheme.colorScheme.onPrimaryContainer
          } else {
            MaterialTheme.colorScheme.onSecondaryContainer
          }
          val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant

          val animatedContentColor by animateColorAsState(
            targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "navItemColor"
          )

          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .onGloballyPositioned { coords ->
                itemWidths[index] = with(density) { coords.size.width.toDp() }
                itemOffsets[index] = with(density) { coords.positionInParent().x.toDp() }
              }
              .clip(pillShape)
              .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                  if (!isSearchItem) {
                    onItemClick(screen, isSelected)
                  }
                }
              )
              .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(screen.titleId),
                tint = animatedContentColor,
                modifier = Modifier
                  .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                  }
                  .size(if (dynamicNavStyle) (if (hideNavLabels) 30.dp else 28.dp) else 26.dp)
              )
            }

            if (!slimNav && !hideNavLabels) {
              Text(
                text = stringResource(screen.titleId),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = animatedContentColor,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              )
            }
          }
        }
      }
    }
  }
}
