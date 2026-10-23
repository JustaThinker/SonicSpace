package com.music.sonic.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class AppIconType(val value: Int) {
  DEFAULT(0),
  SLATE_BLUE(1),
  OLIVE_GREEN(2),
  PLUM_PURPLE(3),
  METALLIC(4),
  MUSIC_KITTY(5),
  WIERD_CAT(6)
}

object IconUtils {
  fun setIcon(context: Context, iconType: AppIconType) {
    val pm = context.packageManager
    val dynamic = ComponentName(context, "com.music.sonic.MainActivityAlias")
    val slateBlue = ComponentName(context, "com.music.sonic.MainActivitySlateBlue")
    val oliveGreen = ComponentName(context, "com.music.sonic.MainActivityOliveGreen")
    val plumPurple = ComponentName(context, "com.music.sonic.MainActivityPlumPurple")
    val metallic = ComponentName(context, "com.music.sonic.MainActivityMetallic")
    val musicKitty = ComponentName(context, "com.music.sonic.MainActivityMusicKitty")
    val wierdCat = ComponentName(context, "com.music.sonic.MainActivityWierdCat")

    val static = ComponentName(context, "com.music.sonic.MainActivityStatic")
    val legacy = ComponentName(context, "com.music.sonic.MainActivityLegacy")
    val cat = ComponentName(context, "com.music.sonic.MainActivityCat")
    val crazyBlue = ComponentName(context, "com.music.sonic.MainActivityCrazyBlue")
    val pookie = ComponentName(context, "com.music.sonic.MainActivityPookie")
    val sky = ComponentName(context, "com.music.sonic.MainActivitySky")
    val echoCat = ComponentName(context, "com.music.sonic.MainActivityEchoCat")
    val eko = ComponentName(context, "com.music.sonic.MainActivityEko")

    val components = listOf(
      dynamic to (iconType == AppIconType.DEFAULT),
      slateBlue to (iconType == AppIconType.SLATE_BLUE),
      oliveGreen to (iconType == AppIconType.OLIVE_GREEN),
      plumPurple to (iconType == AppIconType.PLUM_PURPLE),
      metallic to (iconType == AppIconType.METALLIC),
      musicKitty to (iconType == AppIconType.MUSIC_KITTY),
      wierdCat to (iconType == AppIconType.WIERD_CAT),
      static to false,
      legacy to false,
      cat to false,
      crazyBlue to false,
      pookie to false,
      sky to false,
      echoCat to false,
      eko to false
    )

    for ((component, isEnabled) in components) {
      try {
        pm.setComponentEnabledSetting(
          component,
          if (isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
          else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP
        )
      } catch (_: Exception) {}
    }
  }
}
