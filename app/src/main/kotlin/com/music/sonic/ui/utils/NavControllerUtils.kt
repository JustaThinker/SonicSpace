package com.music.sonic.ui.utils

import androidx.navigation.NavController
import com.music.sonic.ui.screens.Screens

fun NavController.backToMain() {
  val mainRoutes = Screens.MainScreens.map { it.route }

  while (
    previousBackStackEntry != null && currentBackStackEntry?.destination?.route !in mainRoutes
  ) {
    popBackStack()
  }
}
