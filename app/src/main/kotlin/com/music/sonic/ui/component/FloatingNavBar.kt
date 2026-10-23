/** vivimusic Project (C) 2026 Licensed under GPL-3.0 | See git history for contributors */
@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.music.sonic.ui.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.sonic.ui.component.floatingtabbar.FloatingTabBar
import com.music.sonic.ui.component.floatingtabbar.FloatingTabBarDefaults
import com.music.sonic.ui.component.floatingtabbar.FloatingTabBarScrollConnection
import com.music.sonic.ui.player.FloatingMiniPlayer
import com.music.sonic.ui.screens.Screens

/**
 * The iOS 26 style floating navigation bar, an alternative to [AppNavigationBar].
 *
 * Collapses to an inline pill while scrolling down (driven by [scrollConnection]) and expands back
 * on scroll up. The search destination is rendered as the standalone circular tab. When the liquid
 * glass effect is enabled for the navigation bar, the tab
 *
 * When [showPlayerAccessory] is true the now playing controls dock into the bar as an accessory (a
 * pill above the tabs when expanded, inline between the tab pill and the search tab when collapsed)
 * and [onAccessoryClick] opens the full player.
 */
@Composable
fun AppFloatingNavBar(
  navigationItems: List<Screens>,
  currentRoute: String?,
  onItemClick: (Screens, Boolean) -> Unit,
  scrollConnection: FloatingTabBarScrollConnection,
  modifier: Modifier = Modifier,
  pureBlack: Boolean = false,
  showPlayerAccessory: Boolean = false,
  onAccessoryClick: () -> Unit = {},
) {

  val glassConfig = LocalGlassEffectConfig.current
  val useGlass = glassConfig.isEnabledFor(GlassComponent.NAV_BAR)
  val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
  
  val backgroundColor = when {
    useGlass -> Color.Transparent
    pureBlack -> Color.Black
    else -> MaterialTheme.colorScheme.surfaceContainerHigh
  }
  
  val glassContentColor = if (isLight) Color.Black else Color.White
  val adaptiveTextColor = if (useGlass) glassContentColor else glassConfig.resolveTextColor()

  val selectedContentColor = when {
    useGlass -> glassContentColor
    pureBlack -> Color.White
    else -> MaterialTheme.colorScheme.primary
  }
  
  val unselectedContentColor = when {
    useGlass -> glassContentColor.copy(alpha = 0.65f)
    pureBlack -> Color.White.copy(alpha = 0.65f)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  val pillShape = remember { RoundedCornerShape(percent = 50) }

  val tabBarContentModifier = if (useGlass) {
    Modifier.liquidGlass(
      config = glassConfig,
      shape = pillShape,
    )
  } else {
    Modifier
  }

  val selectedTabKey =
    navigationItems
      .firstOrNull { screen -> isRouteSelected(currentRoute, screen.route, navigationItems) }
      ?.route

  val searchScreen = navigationItems.firstOrNull { it == Screens.Search } ?: Screens.Search
  val tabScreens = remember(navigationItems) { navigationItems.filter { it != Screens.Search } }

  val accessoryContentColor =
    when {
      useGlass -> glassContentColor
      pureBlack -> Color.White
      else -> MaterialTheme.colorScheme.onSurface
    }
  val inlineAccessory:
    (@Composable
    SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)? =
    if (showPlayerAccessory) {
      { accessoryModifier, _ ->
        FloatingMiniPlayer(
          isInline = true,
          contentColor = accessoryContentColor,
          onClick = onAccessoryClick,
          modifier = accessoryModifier.then(tabBarContentModifier),
        )
      }
    } else {
      null
    }
  val expandedAccessory:
    (@Composable
    SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)? =
    if (showPlayerAccessory) {
      { accessoryModifier, _ ->
        FloatingMiniPlayer(
          isInline = false,
          contentColor = accessoryContentColor,
          onClick = onAccessoryClick,
          modifier = accessoryModifier.fillMaxWidth().then(tabBarContentModifier),
        )
      }
    } else {
      null
    }

  FloatingTabBar(
    selectedTabKey = selectedTabKey,
    scrollConnection = scrollConnection,
    modifier = modifier,
    tabBarContentModifier = tabBarContentModifier,
    inlineAccessory = inlineAccessory,
    expandedAccessory = expandedAccessory,
    colors =
      FloatingTabBarDefaults.colors(
        backgroundColor = backgroundColor,
        accessoryBackgroundColor = backgroundColor,
        indicatorColor = if (useGlass) {
          (if (isLight) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.22f))
        } else {
          MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        },
      ),
    elevations = if (useGlass) {
      FloatingTabBarDefaults.elevations(
        inlineElevation = 0.dp,
        expandedElevation = 0.dp,
      )
    } else {
      FloatingTabBarDefaults.elevations()
    },
    sizes =
      FloatingTabBarDefaults.sizes(
        tabBarContentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp),
        tabExpandedContentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 9.dp, horizontal = 12.dp),
        componentSpacing = 8.dp,
      ),
    // The tab content lambdas are captured once per contentKey, so anything they
    // close over (selection, colors) must be part of the key to avoid stale UI.
    contentKey =
      listOf(
        selectedTabKey,
        navigationItems,
        selectedContentColor,
        unselectedContentColor,
      ),
  ) {
    tabScreens.forEach { screen ->
      val isSelected = screen.route == selectedTabKey
      tab(
        key = screen.route,
        title = {
          val animatedContentColor by animateColorAsState(
            targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "tabTextColor_${screen.route}"
          )
          Text(
            text = stringResource(screen.titleId),
            color = animatedContentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
          )
        },
        icon = {
          val animatedContentColor by animateColorAsState(
            targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "tabIconColor_${screen.route}"
          )
          Icon(
            painter =
              painterResource(if (isSelected) screen.iconIdActive else screen.iconIdInactive),
            contentDescription = stringResource(screen.titleId),
            tint = animatedContentColor,
            modifier = Modifier.size(24.dp),
          )
        },
        onClick = { onItemClick(screen, isSelected) },
      )
    }

    val isSearchSelected = searchScreen.route == selectedTabKey
    standaloneTab(
      key = searchScreen.route,
      icon = {
        val animatedSearchColor by animateColorAsState(
          targetValue = if (isSearchSelected) selectedContentColor else unselectedContentColor,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
          label = "searchContentColor"
        )
        Icon(
          painter =
            painterResource(if (isSearchSelected) searchScreen.iconIdActive else searchScreen.iconIdInactive),
          contentDescription = stringResource(searchScreen.titleId),
          tint = animatedSearchColor,
          modifier = Modifier.size(24.dp),
        )
      },
      onClick = { onItemClick(searchScreen, isSearchSelected) },
    )
  }
}
