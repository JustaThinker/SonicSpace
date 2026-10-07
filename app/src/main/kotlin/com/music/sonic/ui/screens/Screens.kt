package com.music.sonic.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.music.sonic.R

@Immutable
sealed class Screens(
  @StringRes val titleId: Int,
  @DrawableRes val iconIdInactive: Int,
  @DrawableRes val iconIdActive: Int,
  val route: String,
) {
  object Home :
    Screens(
      titleId = R.string.home,
      iconIdInactive = R.drawable.ic_explore,
      iconIdActive = R.drawable.ic_explore,
      route = "home"
    )

  object Search :
    Screens(
      titleId = R.string.search,
      iconIdInactive = R.drawable.search,
      iconIdActive = R.drawable.search,
      route = "search_input"
    )

  object ListenTogether :
    Screens(
      titleId = R.string.together,
      iconIdInactive = R.drawable.group_outlined,
      iconIdActive = R.drawable.group_filled,
      route = "listen_together"
    )

  object Library :
    Screens(
      titleId = R.string.filter_library,
      iconIdInactive = R.drawable.ic_library_music,
      iconIdActive = R.drawable.ic_library_music,
      route = "library"
    )

  object Effects :
    Screens(
      titleId = R.string.effects,
      iconIdInactive = R.drawable.ic_equalizer,
      iconIdActive = R.drawable.ic_equalizer,
      route = "effects"
    )

  object Settings :
    Screens(
      titleId = R.string.settings,
      iconIdInactive = R.drawable.settings,
      iconIdActive = R.drawable.settings,
      route = "settings"
    )

  companion object {
    val MainScreens = listOf(Home, Search, ListenTogether, Library, Effects)
  }
}
