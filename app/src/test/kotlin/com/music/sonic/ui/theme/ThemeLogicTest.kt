package com.music.sonic.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeLogicTest {
  @Test
  fun defaultThemeUsesSystemDynamicFallback() {
    assertTrue(
      shouldUseSystemDynamicColor(
        themeColor = DefaultThemeColor,
        dynamicAlbumColor = null,
        liquidGlassEnabled = false,
        bitChordPaletteEnabled = false,
      )
    )
  }

  @Test
  fun customThemeColorDoesNotUseSystemDynamicFallback() {
    assertFalse(
      shouldUseSystemDynamicColor(
        themeColor = Color(0xFF7C4DFF),
        dynamicAlbumColor = null,
        liquidGlassEnabled = false,
        bitChordPaletteEnabled = false,
      )
    )
  }

  @Test
  fun lockedThemePaletteDisablesSystemDynamicFallback() {
    assertFalse(
      shouldUseSystemDynamicColor(
        themeColor = DefaultThemeColor,
        dynamicAlbumColor = null,
        liquidGlassEnabled = true,
        bitChordPaletteEnabled = false,
      )
    )
  }
}
