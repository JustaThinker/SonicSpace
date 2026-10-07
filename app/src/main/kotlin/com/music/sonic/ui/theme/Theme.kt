@file:Suppress("UNCHECKED_CAST")

package com.music.sonic.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat.getInsetsController
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score

@Composable
fun <T> SharedPreferences.observeKey(key: String, defaultValue: T): State<T> {
  val state = remember { mutableStateOf(defaultValue) }
  DisposableEffect(this, key) {
    state.value = when (defaultValue) {
      is Boolean -> getBoolean(key, defaultValue) as T
      is String -> getString(key, defaultValue) as T
      is Float -> getFloat(key, defaultValue) as T
      is Int -> getInt(key, defaultValue) as T
      is Long -> getLong(key, defaultValue) as T
      else -> defaultValue
    }
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
      if (changedKey == key) {
        state.value = when (defaultValue) {
          is Boolean -> prefs.getBoolean(key, defaultValue) as T
          is String -> prefs.getString(key, defaultValue) as T
          is Float -> prefs.getFloat(key, defaultValue) as T
          is Int -> prefs.getInt(key, defaultValue) as T
          is Long -> prefs.getLong(key, defaultValue) as T
          else -> defaultValue
        }
      }
    }
    registerOnSharedPreferenceChangeListener(listener)
    onDispose {
      unregisterOnSharedPreferenceChangeListener(listener)
    }
  }
  return state
}

val DefaultThemeColor = Color(0xFF1E88E5)

private fun ColorScheme.softenDynamicColors(darkTheme: Boolean): ColorScheme {
  fun soften(color: Color, maxSat: Float = 0.42f): Color {
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(color.toArgb(), hsl)
    val h = hsl[0]
    var s = hsl[1]
    var l = hsl[2]

    val isHarshHue = (h in 0f..40f || h in 320f..360f || h in 70f..165f)
    val targetMaxSat = if (isHarshHue) (maxSat * 0.85f) else maxSat

    if (s > targetMaxSat) {
      s = targetMaxSat
    }

    if (darkTheme) {
      if (l < 0.75f) {
        l = (l * 1.15f).coerceIn(0.75f, 0.88f)
      }
    }
    // Light mode: keep original lightness — Material dynamic color already provides
    // proper contrast-safe lightness values; boosting them washes out the palette.

    return Color(androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(h, s, l)))
  }

  return copy(
    primary = soften(primary),
    secondary = soften(secondary),
    tertiary = soften(tertiary),
    error = soften(error, maxSat = 0.38f),
    primaryContainer = if (darkTheme) soften(primaryContainer, maxSat = 0.35f) else primaryContainer,
    secondaryContainer = if (darkTheme) soften(secondaryContainer, maxSat = 0.30f) else secondaryContainer,
    tertiaryContainer = if (darkTheme) soften(tertiaryContainer, maxSat = 0.35f) else tertiaryContainer,
  )
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpatialFlowTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicAlbumColor: Int? = null,
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val prefs = remember(context) {
    context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
  }
  val amoledEnabled by prefs.observeKey("amoled_black", false)
  val albumArtThemeEnabled by prefs.observeKey("dynamic_album_theme", true)

  val colorScheme = remember(darkTheme, amoledEnabled, albumArtThemeEnabled, dynamicAlbumColor) {
    val baseScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (darkTheme) dynamicDarkColorScheme(context).softenDynamicColors(darkTheme)
      else dynamicLightColorScheme(context).softenDynamicColors(darkTheme)
    } else {
      if (darkTheme) darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        primaryContainer = PrimaryContainer,
        onPrimaryContainer = OnPrimaryContainer,
        background = Surface,
        onBackground = OnSurface,
        surface = Surface,
        onSurface = OnSurface,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = OnSurfaceVariant
      ).softenDynamicColors(darkTheme)
      else lightColorScheme().softenDynamicColors(darkTheme)
    }

    if (dynamicAlbumColor != null) {
      val seedColor = Color(dynamicAlbumColor)
      val hsl = FloatArray(3)
      androidx.core.graphics.ColorUtils.colorToHSL(seedColor.toArgb(), hsl)

      val isMonochrome = hsl[1] < 0.06f
      val baseHue = hsl[0]
      val rawSat = if (isMonochrome) 0f else hsl[1]

      val isHarshHue = (baseHue in 0f..40f || baseHue in 320f..360f || baseHue in 70f..165f)
      val satCap = if (isHarshHue) 0.35f else 0.42f
      val baseSat = (rawSat * 0.45f).coerceIn(0.10f, satCap)

      val primarySat = if (isMonochrome) 0f else baseSat.coerceIn(0.12f, satCap)
      val primaryContainerSat = if (isMonochrome) 0f else (baseSat * 0.7f).coerceIn(0.08f, satCap * 0.8f)
      val secondarySat = if (isMonochrome) 0f else (baseSat * 0.6f).coerceIn(0.08f, satCap * 0.7f)
      val secondaryContainerSat = if (isMonochrome) 0f else (baseSat * 0.5f).coerceIn(0.06f, satCap * 0.6f)
      val tertiarySat = if (isMonochrome) 0f else (baseSat * 0.7f).coerceIn(0.10f, satCap * 0.8f)
      val tertiaryContainerSat = if (isMonochrome) 0f else (baseSat * 0.6f).coerceIn(0.08f, satCap * 0.7f)

      fun colorAt(h: Float, s: Float, l: Float): Color {
        return Color(
          androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(h, s, l))
        )
      }

      if (darkTheme) {
        val bgSat = (baseSat * 0.12f).coerceIn(0.02f, 0.08f)
        val dynamicBg = colorAt(baseHue, bgSat, 0.07f)
        val dynamicSurface = colorAt(baseHue, bgSat, 0.09f)
        val dynamicContainer = colorAt(baseHue, bgSat, 0.12f)

        baseScheme.copy(
          background = if (amoledEnabled) Color.Black else dynamicBg,
          surface = if (amoledEnabled) Color.Black else dynamicSurface,
          surfaceContainer = if (amoledEnabled) Color.Black else dynamicContainer,
          surfaceContainerLow = if (amoledEnabled) Color.Black else colorAt(baseHue, bgSat, 0.08f),
          surfaceContainerLowest = if (amoledEnabled) Color.Black else colorAt(baseHue, bgSat, 0.05f),
          surfaceContainerHigh = if (amoledEnabled) Color(0xFF0D0D0D) else colorAt(baseHue, bgSat, 0.15f),
          surfaceContainerHighest = if (amoledEnabled) Color(0xFF141414) else colorAt(baseHue, bgSat, 0.18f),

          onBackground = colorAt(baseHue, bgSat, 0.90f),
          onSurface = colorAt(baseHue, bgSat, 0.90f),

          primary = colorAt(baseHue, primarySat, 0.82f),
          onPrimary = colorAt(baseHue, primarySat, 0.15f),
          primaryContainer = colorAt(baseHue, primaryContainerSat, 0.22f),
          onPrimaryContainer = colorAt(baseHue, primaryContainerSat, 0.92f),

          secondary = colorAt(baseHue, secondarySat, 0.78f),
          onSecondary = colorAt(baseHue, secondarySat, 0.15f),
          secondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.20f),
          onSecondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.88f),

          tertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.78f),
          onTertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.15f),
          tertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.20f),
          onTertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.88f),

          outline = colorAt(baseHue, bgSat, 0.60f),
          outlineVariant = colorAt(baseHue, bgSat, 0.30f),

          error = colorAt(0f, 0.35f, 0.75f),
          onError = colorAt(0f, 0.35f, 0.15f),
          errorContainer = colorAt(0f, 0.35f, 0.22f),
          onErrorContainer = colorAt(0f, 0.35f, 0.92f)
        )
      } else {
        val bgSat = (baseSat * 0.15f).coerceIn(0.02f, 0.10f)

        baseScheme.copy(
          background = colorAt(baseHue, bgSat, 0.97f),
          surface = colorAt(baseHue, bgSat, 0.99f),
          surfaceContainer = colorAt(baseHue, bgSat, 0.93f),
          surfaceContainerLow = colorAt(baseHue, bgSat, 0.95f),
          surfaceContainerLowest = colorAt(baseHue, bgSat, 1.00f),
          surfaceContainerHigh = colorAt(baseHue, bgSat, 0.90f),
          surfaceContainerHighest = colorAt(baseHue, bgSat, 0.87f),
          surfaceVariant = colorAt(baseHue, (bgSat * 1.4f).coerceAtMost(0.14f), 0.90f),

          onBackground = colorAt(baseHue, bgSat, 0.08f),
          onSurface = colorAt(baseHue, bgSat, 0.08f),
          onSurfaceVariant = colorAt(baseHue, bgSat * 0.8f, 0.35f),

          primary = colorAt(baseHue, primarySat, 0.38f),
          onPrimary = Color.White,
          primaryContainer = colorAt(baseHue, primaryContainerSat, 0.91f),
          onPrimaryContainer = colorAt(baseHue, primaryContainerSat, 0.12f),

          secondary = colorAt(baseHue, secondarySat, 0.42f),
          onSecondary = Color.White,
          secondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.91f),
          onSecondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.12f),

          tertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.42f),
          onTertiary = Color.White,
          tertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.91f),
          onTertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.12f),

          outline = colorAt(baseHue, bgSat, 0.55f),
          outlineVariant = colorAt(baseHue, bgSat, 0.80f),

          error = colorAt(0f, 0.55f, 0.40f),
          onError = Color.White,
          errorContainer = colorAt(0f, 0.35f, 0.92f),
          onErrorContainer = colorAt(0f, 0.35f, 0.12f)
        )
      }
    } else if (darkTheme && amoledEnabled) {
      baseScheme.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainer = Color.Black,
        surfaceContainerHigh = Color(0xFF0D0D0D),
        surfaceContainerHighest = Color(0xFF141414),
        surfaceContainerLow = Color.Black,
        surfaceContainerLowest = Color.Black
      )
    } else {
      baseScheme
    }
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val activity = view.context.findActivity()
      activity?.window?.let { window ->
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  MaterialExpressiveTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    motionScheme = MotionScheme.expressive(),
    content = content
  )
}

val ColorSaver =
  object : androidx.compose.runtime.saveable.Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)

    override fun androidx.compose.runtime.saveable.SaverScope.save(value: Color): Int = value.toArgb()
  }

@Composable
fun echomusicTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  pureBlack: Boolean = false,
  themeColor: Color = DefaultThemeColor,
  content: @Composable () -> Unit,
) {
  SpatialFlowTheme(
    darkTheme = darkTheme,
    dynamicAlbumColor = themeColor.toArgb(),
    content = content
  )
}

fun Bitmap.extractThemeColor(): Color {
  val colorsToPopulation =
    Palette.from(this).maximumColorCount(8).generate().swatches.associate {
      it.rgb to it.population
    }
  val rankedColors = Score.score(colorsToPopulation)
  return Color(rankedColors.first())
}

fun Bitmap.extractGradientColors(): List<Color> {
  val extractedColors =
    Palette.from(this).maximumColorCount(64).generate().swatches.associate {
      it.rgb to it.population
    }

  val orderedColors =
    Score.score(extractedColors, 2, 0xff4285f4.toInt(), true).sortedByDescending {
      Color(it).luminance()
    }

  return if (orderedColors.size >= 2) listOf(Color(orderedColors[0]), Color(orderedColors[1]))
  else listOf(Color(0xFF595959), Color(0xFF0D0D0D))
}

fun ColorScheme.pureBlack(apply: Boolean) =
  if (apply) copy(surface = Color.Black, background = Color.Black) else this

internal tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}
