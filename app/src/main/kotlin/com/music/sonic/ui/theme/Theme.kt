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
  val defaultMaxSat = if (darkTheme) 0.42f else 0.85f
  fun soften(color: Color, maxSat: Float = defaultMaxSat): Color {
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
    error = soften(error, maxSat = if (darkTheme) 0.38f else 0.80f),
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
  amoledMode: Boolean = false,
  dynamicAlbumColor: Int? = null,
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val prefs = remember(context) {
    context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
  }
  val amoledPref by prefs.observeKey("amoled_black", false)
  val albumArtThemeEnabled by prefs.observeKey("dynamic_album_theme", true)
  val isAmoled = amoledMode || amoledPref

  val colorScheme = remember(darkTheme, isAmoled, albumArtThemeEnabled, dynamicAlbumColor) {
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

    val finalScheme = if (darkTheme && isAmoled && !(albumArtThemeEnabled && dynamicAlbumColor != null)) {
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

    if (albumArtThemeEnabled && dynamicAlbumColor != null) {
      val seedColor = Color(dynamicAlbumColor)
      val hsl = FloatArray(3)
      androidx.core.graphics.ColorUtils.colorToHSL(seedColor.toArgb(), hsl)

      val isMonochrome = hsl[1] < 0.06f
      val baseHue = hsl[0]
      val rawSat = if (isMonochrome) 0f else hsl[1]

      // Calibrate saturation to SpatialFlow's exact subdued range and avoid harsh glare
      val isHarshHue = (baseHue in 0f..40f || baseHue in 320f..360f || baseHue in 70f..165f)
      val maxAllowedSat = if (isHarshHue) 0.32f else 0.38f
      val baseSat = (rawSat * 0.65f).coerceIn(0.14f, maxAllowedSat)

      // Saturation calculations calibrated to soft, comfortable Material 3 tones
      val primarySat = if (isMonochrome) 0f else (baseSat * 1.25f).coerceIn(0.32f, 0.46f)
      val primaryContainerSat = if (isMonochrome) 0f else (baseSat * 0.85f).coerceIn(0.20f, 0.34f)
      val secondarySat = if (isMonochrome) 0f else (baseSat * 0.55f).coerceIn(0.14f, 0.25f)
      val secondaryContainerSat = if (isMonochrome) 0f else (baseSat * 0.45f).coerceIn(0.10f, 0.20f)
      val tertiarySat = if (isMonochrome) 0f else (baseSat * 0.65f).coerceIn(0.18f, 0.30f)
      val tertiaryContainerSat = if (isMonochrome) 0f else (baseSat * 0.50f).coerceIn(0.14f, 0.24f)

      fun colorAt(h: Float, s: Float, l: Float): Color {
        return Color(
          androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(h, s, l))
        )
      }

      val finalDynamicScheme = if (darkTheme) {
        // Dark Theme Tones: gentle background tint, never harsh
        val bgSat = (baseSat * 0.12f).coerceIn(0.015f, 0.045f)

        finalScheme.copy(
          background = colorAt(baseHue, bgSat, 0.04f),
          onBackground = colorAt(baseHue, bgSat, 0.90f),

          surface = colorAt(baseHue, bgSat, 0.06f),
          onSurface = colorAt(baseHue, bgSat, 0.90f),

          surfaceContainerLowest = colorAt(baseHue, bgSat, 0.02f),
          surfaceContainerLow = colorAt(baseHue, bgSat, 0.08f),
          surfaceContainer = colorAt(baseHue, bgSat, 0.12f),
          surfaceContainerHigh = colorAt(baseHue, bgSat, 0.16f),
          surfaceContainerHighest = colorAt(baseHue, bgSat, 0.20f),

          surfaceVariant = colorAt(baseHue, bgSat, 0.25f),
          onSurfaceVariant = colorAt(baseHue, bgSat, 0.80f),

          primary = colorAt(baseHue, primarySat, 0.76f),
          onPrimary = colorAt(baseHue, primarySat, 0.18f),
          primaryContainer = colorAt(baseHue, primaryContainerSat, 0.24f),
          onPrimaryContainer = colorAt(baseHue, primaryContainerSat, 0.90f),

          secondary = colorAt(baseHue, secondarySat, 0.72f),
          onSecondary = colorAt(baseHue, secondarySat, 0.18f),
          secondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.20f),
          onSecondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.86f),

          tertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.72f),
          onTertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.18f),
          tertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.20f),
          onTertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.86f),

          outline = colorAt(baseHue, bgSat, 0.55f),
          outlineVariant = colorAt(baseHue, bgSat, 0.28f),

          error = colorAt(0f, 0.40f, 0.65f),
          onError = colorAt(0f, 0.40f, 0.18f),
          errorContainer = colorAt(0f, 0.40f, 0.24f),
          onErrorContainer = colorAt(0f, 0.40f, 0.90f)
        )
      } else {
        // Light Theme Tones: high contrast, soft subtle background
        val bgSat = (baseSat * 0.12f).coerceIn(0.015f, 0.05f)

        finalScheme.copy(
          background = colorAt(baseHue, bgSat, 0.98f),
          onBackground = colorAt(baseHue, bgSat, 0.10f),

          surface = colorAt(baseHue, bgSat, 0.98f),
          onSurface = colorAt(baseHue, bgSat, 0.10f),

          surfaceContainerLowest = colorAt(baseHue, bgSat, 1.0f),
          surfaceContainerLow = colorAt(baseHue, bgSat, 0.96f),
          surfaceContainer = colorAt(baseHue, bgSat, 0.94f),
          surfaceContainerHigh = colorAt(baseHue, bgSat, 0.90f),
          surfaceContainerHighest = colorAt(baseHue, bgSat, 0.86f),

          surfaceVariant = colorAt(baseHue, bgSat, 0.82f),
          onSurfaceVariant = colorAt(baseHue, bgSat, 0.32f),

          primary = colorAt(baseHue, primarySat, 0.42f),
          onPrimary = colorAt(baseHue, primarySat, 0.98f),
          primaryContainer = colorAt(baseHue, primaryContainerSat, 0.88f),
          onPrimaryContainer = colorAt(baseHue, primaryContainerSat, 0.12f),

          secondary = colorAt(baseHue, secondarySat, 0.45f),
          onSecondary = colorAt(baseHue, secondarySat, 0.98f),
          secondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.88f),
          onSecondaryContainer = colorAt(baseHue, secondaryContainerSat, 0.12f),

          tertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.45f),
          onTertiary = colorAt((baseHue + 60f) % 360f, tertiarySat, 0.98f),
          tertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.88f),
          onTertiaryContainer = colorAt((baseHue + 60f) % 360f, tertiaryContainerSat, 0.12f),

          outline = colorAt(baseHue, bgSat, 0.50f),
          outlineVariant = colorAt(baseHue, bgSat, 0.80f),

          error = colorAt(0f, 0.50f, 0.45f),
          onError = colorAt(0f, 0.50f, 0.98f),
          errorContainer = colorAt(0f, 0.40f, 0.88f),
          onErrorContainer = colorAt(0f, 0.40f, 0.12f)
        )
      }

      // AMOLED black override for dynamic theme
      if (darkTheme && isAmoled) {
        finalDynamicScheme.copy(
          background = Color.Black,
          surface = Color.Black,
          surfaceContainerLowest = Color.Black,
          surfaceContainerLow = Color.Black,
          surfaceContainer = Color.Black
        )
      } else {
        finalDynamicScheme
      }
    } else {
      finalScheme
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
  dynamicAlbumColor: Int? = null,
  themeColor: Color = DefaultThemeColor,
  content: @Composable () -> Unit,
) {
  SpatialFlowTheme(
    darkTheme = darkTheme,
    amoledMode = pureBlack,
    dynamicAlbumColor = dynamicAlbumColor ?: (if (themeColor != DefaultThemeColor) themeColor.toArgb() else null),
    content = content
  )
}

fun Bitmap.extractThemeColor(): Color {
  val palette = Palette.from(this).maximumColorCount(24).generate()
  val allSwatches = palette.swatches
  if (allSwatches.isEmpty()) return DefaultThemeColor

  val totalPopulation = allSwatches.sumOf { it.population }.coerceAtLeast(1)
  val candidateSwatches = listOfNotNull(
    palette.vibrantSwatch,
    palette.darkVibrantSwatch,
    palette.lightVibrantSwatch,
    palette.mutedSwatch,
    palette.darkMutedSwatch,
    palette.lightMutedSwatch,
    palette.dominantSwatch
  )
  val bestAccentSwatch = candidateSwatches.maxByOrNull { swatch ->
    val sat = swatch.hsl[1]
    val pop = swatch.population.toFloat() / totalPopulation.toFloat()
    sat * (0.25f + pop)
  }
  val accentColor = bestAccentSwatch?.rgb
    ?: palette.getVibrantColor(palette.getDominantColor(0xFF8338EC.toInt()))

  val baseBgColor = palette.getDominantColor(0xFF0F0F0F.toInt())

  val accentHsl = FloatArray(3)
  val bgHsl = FloatArray(3)
  androidx.core.graphics.ColorUtils.colorToHSL(accentColor, accentHsl)
  androidx.core.graphics.ColorUtils.colorToHSL(baseBgColor, bgHsl)

  val maxChannelDelta = maxOf(
    kotlin.math.abs(android.graphics.Color.red(baseBgColor) - android.graphics.Color.green(baseBgColor)),
    kotlin.math.abs(android.graphics.Color.green(baseBgColor) - android.graphics.Color.blue(baseBgColor)),
    kotlin.math.abs(android.graphics.Color.blue(baseBgColor) - android.graphics.Color.red(baseBgColor))
  ) / 255f

  val colorfulPopulation = allSwatches
    .filter { it.hsl[1] >= 0.16f }
    .sumOf { it.population }
  val colorfulRatio = colorfulPopulation.toFloat() / totalPopulation.toFloat()
  val isMonochromatic = (
    colorfulRatio < 0.08f &&
      bgHsl[1] < 0.14f &&
      accentHsl[1] < 0.18f &&
      maxChannelDelta < 0.09f
  ) || (bgHsl[1] < 0.06f && accentHsl[1] < 0.08f)

  val finalBg = if (isMonochromatic) {
    bgHsl[0] = 0f
    bgHsl[1] = 0f
    bgHsl[2] = 0.30f
    androidx.core.graphics.ColorUtils.HSLToColor(bgHsl)
  } else {
    if (bgHsl[1] < 0.16f) {
      bgHsl[0] = accentHsl[0]
      bgHsl[1] = (accentHsl[1] * 0.36f).coerceIn(0.18f, 0.40f)
    } else {
      bgHsl[1] = bgHsl[1].coerceIn(0.18f, 0.60f)
    }
    bgHsl[2] = bgHsl[2].coerceIn(0.22f, 0.48f)
    androidx.core.graphics.ColorUtils.HSLToColor(bgHsl)
  }

  return Color(finalBg)
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
