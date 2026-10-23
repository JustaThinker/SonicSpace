@file:Suppress("UNCHECKED_CAST")

package com.music.sonic.ui.theme

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Base64
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat.getInsetsController
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.toHct
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.math.min

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

val DefaultThemeColor = Color(0xFF1565C0)

data class ThemeSeedPalette(
  val primary: Color,
  val secondary: Color,
  val tertiary: Color,
  val neutral: Color,
)

internal fun shouldUseSystemDynamicColor(
  themeColor: Color = DefaultThemeColor,
  dynamicAlbumColor: Int? = null,
  liquidGlassEnabled: Boolean = false,
  bitChordPaletteEnabled: Boolean = false,
  enableDynamicTheme: Boolean = true,
): Boolean =
  enableDynamicTheme &&
    !liquidGlassEnabled &&
    !bitChordPaletteEnabled &&
    dynamicAlbumColor == null &&
    themeColor == DefaultThemeColor

fun paletteStyleFor(seedColor: Color): PaletteStyle {
  val chroma = seedColor.toHct().chroma
  return when {
    chroma < 4.0 -> PaletteStyle.Monochrome
    chroma < 12.0 -> PaletteStyle.Neutral
    else -> PaletteStyle.TonalSpot
  }
}

fun materialKolorScheme(
  seedColor: Color,
  isDark: Boolean,
  contrastLevel: Double = 0.0,
  style: PaletteStyle,
): ColorScheme =
  dynamicColorScheme(
    seedColor = seedColor,
    isDark = isDark,
    contrastLevel = contrastLevel,
    style = style,
  )

fun mergedSeedColorScheme(
  primarySeed: Color,
  secondarySeed: Color,
  tertiarySeed: Color,
  neutralSeed: Color,
  isDark: Boolean,
  contrastLevel: Double = 0.0,
  style: PaletteStyle = paletteStyleFor(primarySeed),
): ColorScheme {
  val primaryScheme = materialKolorScheme(primarySeed, isDark, contrastLevel, style)
  val secondaryScheme = materialKolorScheme(secondarySeed, isDark, contrastLevel, paletteStyleFor(secondarySeed))
  val tertiaryScheme = materialKolorScheme(tertiarySeed, isDark, contrastLevel, paletteStyleFor(tertiarySeed))
  val neutralScheme = materialKolorScheme(neutralSeed, isDark, contrastLevel, paletteStyleFor(neutralSeed))

  return ColorScheme(
    primary = primaryScheme.primary,
    onPrimary = primaryScheme.onPrimary,
    primaryContainer = primaryScheme.primaryContainer,
    onPrimaryContainer = primaryScheme.onPrimaryContainer,
    inversePrimary = primaryScheme.inversePrimary,
    secondary = secondaryScheme.primary,
    onSecondary = secondaryScheme.onPrimary,
    secondaryContainer = secondaryScheme.primaryContainer,
    onSecondaryContainer = secondaryScheme.onPrimaryContainer,
    tertiary = tertiaryScheme.primary,
    onTertiary = tertiaryScheme.onPrimary,
    tertiaryContainer = tertiaryScheme.primaryContainer,
    onTertiaryContainer = tertiaryScheme.onPrimaryContainer,
    background = neutralScheme.background,
    onBackground = neutralScheme.onBackground,
    surface = neutralScheme.surface,
    onSurface = neutralScheme.onSurface,
    surfaceVariant = neutralScheme.surfaceVariant,
    onSurfaceVariant = neutralScheme.onSurfaceVariant,
    inverseSurface = neutralScheme.inverseSurface,
    inverseOnSurface = neutralScheme.inverseOnSurface,
    surfaceBright = neutralScheme.surfaceBright,
    surfaceDim = neutralScheme.surfaceDim,
    surfaceContainer = neutralScheme.surfaceContainer,
    surfaceContainerLow = neutralScheme.surfaceContainerLow,
    surfaceContainerLowest = neutralScheme.surfaceContainerLowest,
    surfaceContainerHigh = neutralScheme.surfaceContainerHigh,
    surfaceContainerHighest = neutralScheme.surfaceContainerHighest,
    outline = neutralScheme.outline,
    outlineVariant = neutralScheme.outlineVariant,
    error = primaryScheme.error,
    onError = primaryScheme.onError,
    errorContainer = primaryScheme.errorContainer,
    onErrorContainer = primaryScheme.onErrorContainer,
    scrim = neutralScheme.scrim,
    surfaceTint = primaryScheme.surfaceTint,
  )
}

fun exactPaletteColorScheme(
  palette: ThemeSeedPalette,
  isDark: Boolean,
): ColorScheme =
  mergedSeedColorScheme(
    primarySeed = palette.primary,
    secondarySeed = palette.secondary,
    tertiarySeed = palette.tertiary,
    neutralSeed = palette.neutral,
    isDark = isDark,
  )

fun materialKolorDynamicColorScheme(
  keyColor: Color,
  isDark: Boolean,
  contrastLevel: Double = 0.0,
  style: PaletteStyle = paletteStyleFor(keyColor),
): ColorScheme =
  mergedSeedColorScheme(
    primarySeed = keyColor,
    secondarySeed = keyColor,
    tertiarySeed = keyColor,
    neutralSeed = keyColor,
    isDark = isDark,
    contrastLevel = contrastLevel,
    style = style,
  )

// BitChord theme palette matching https://github.com/kushagrasinghx/BitChord
val BitChordAccentRed = Color(0xFFFA2D48)

val BitChordDarkColors = darkColorScheme(
  primary = Color.White,
  onPrimary = Color.Black,
  background = Color.Black,
  onBackground = Color.White,
  surface = Color(0xFF0D0D0F),
  onSurface = Color.White,
  surfaceVariant = Color(0xFF1C1C1E),
  onSurfaceVariant = Color(0xFF8E8E93),
  outline = Color(0xFF2C2C2E),
  outlineVariant = Color(0xFF3A3A3C),
  surfaceContainerLowest = Color.Black,
  surfaceContainerLow = Color(0xFF08080A),
  surfaceContainer = Color(0xFF0D0D0F),
  surfaceContainerHigh = Color(0xFF141416),
  surfaceContainerHighest = Color(0xFF1C1C1E),
  primaryContainer = Color(0xFF1C1C1E),
  onPrimaryContainer = Color.White,
  secondary = Color.White,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF1C1C1E),
  onSecondaryContainer = Color.White,
  tertiary = BitChordAccentRed,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF2C1014),
  onTertiaryContainer = BitChordAccentRed,
)

val BitChordLightColors = lightColorScheme(
  primary = Color.Black,
  onPrimary = Color.White,
  background = Color.White,
  onBackground = Color.Black,
  surface = Color(0xFFF7F7F9),
  onSurface = Color.Black,
  surfaceVariant = Color(0xFFF2F2F7),
  onSurfaceVariant = Color(0xFF6E6E73),
  outline = Color(0xFFE5E5EA),
  outlineVariant = Color(0xFFD1D1D6),
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = Color(0xFFFBFBFD),
  surfaceContainer = Color(0xFFF7F7F9),
  surfaceContainerHigh = Color(0xFFEDEDF2),
  surfaceContainerHighest = Color(0xFFE5E5EA),
  primaryContainer = Color(0xFFF2F2F7),
  onPrimaryContainer = Color.Black,
  secondary = Color.Black,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF2F2F7),
  onSecondaryContainer = Color.Black,
  tertiary = BitChordAccentRed,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFFFE5E8),
  onTertiaryContainer = BitChordAccentRed,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object DisabledMotionScheme : MotionScheme {
  override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = snap()
  override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = snap()
  override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = snap()
  override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = snap()
  override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = snap()
  override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = snap()
}

val ExpressiveShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun animateColorScheme(
  targetColorScheme: ColorScheme,
  animationSpec: FiniteAnimationSpec<Color> = MotionScheme.expressive().defaultEffectsSpec(),
): ColorScheme =
  ColorScheme(
    primary = animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary").value,
    onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary").value,
    primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer").value,
    onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value,
    inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "inversePrimary").value,
    secondary = animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary").value,
    onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary").value,
    secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer").value,
    onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer").value,
    tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary").value,
    onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary").value,
    tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer").value,
    onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer").value,
    background = animateColorAsState(targetColorScheme.background, animationSpec, label = "background").value,
    onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground").value,
    surface = animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface").value,
    onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface").value,
    surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant").value,
    onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant").value,
    surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, animationSpec, label = "surfaceTint").value,
    inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "inverseSurface").value,
    inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "inverseOnSurface").value,
    error = animateColorAsState(targetColorScheme.error, animationSpec, label = "error").value,
    onError = animateColorAsState(targetColorScheme.onError, animationSpec, label = "onError").value,
    errorContainer = animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "errorContainer").value,
    onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "onErrorContainer").value,
    outline = animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline").value,
    outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "outlineVariant").value,
    scrim = animateColorAsState(targetColorScheme.scrim, animationSpec, label = "scrim").value,
    surfaceBright = animateColorAsState(targetColorScheme.surfaceBright, animationSpec, label = "surfaceBright").value,
    surfaceDim = animateColorAsState(targetColorScheme.surfaceDim, animationSpec, label = "surfaceDim").value,
    surfaceContainer = animateColorAsState(targetColorScheme.surfaceContainer, animationSpec, label = "surfaceContainer").value,
    surfaceContainerLow = animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec, label = "surfaceContainerLow").value,
    surfaceContainerLowest = animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec, label = "surfaceContainerLowest").value,
    surfaceContainerHigh = animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec, label = "surfaceContainerHigh").value,
    surfaceContainerHighest = animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec, label = "surfaceContainerHighest").value,
  )

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpatialFlowTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  amoledMode: Boolean = false,
  dynamicAlbumColor: Int? = null,
  themeColor: Color = DefaultThemeColor,
  seedPalette: ThemeSeedPalette? = null,
  disableAnimations: Boolean = false,
  liquidGlassEnabled: Boolean = false,
  bitChordPaletteEnabled: Boolean = false,
  enableDynamicTheme: Boolean = true,
  appFont: String = "GOOGLE_SANS",
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val prefs = remember(context) {
    context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
  }
  val amoledPref by prefs.observeKey("amoled_black", false)
  val albumArtThemeEnabled by prefs.observeKey("dynamic_album_theme", true)
  val bitChordPalettePref by prefs.observeKey("bitchord_palette", false)
  val appFontPref by prefs.observeKey("app_font", appFont)
  val activeFont = if (appFont != "GOOGLE_SANS") appFont else appFontPref
  val typography = remember(activeFont) { typographyFor(activeFont) }
  val isAmoled = amoledMode || amoledPref
  val isBitChord = liquidGlassEnabled || bitChordPaletteEnabled

  val motionScheme = remember(disableAnimations) {
    if (disableAnimations) DisabledMotionScheme else MotionScheme.expressive()
  }

  val targetColorScheme = remember(
    darkTheme,
    isAmoled,
    albumArtThemeEnabled,
    dynamicAlbumColor,
    isBitChord,
    themeColor,
    seedPalette,
    liquidGlassEnabled,
    bitChordPaletteEnabled,
  ) {
    if (isBitChord) {
      if (darkTheme) {
        if (isAmoled) BitChordDarkColors.copy(surface = Color.Black, surfaceContainer = Color.Black)
        else BitChordDarkColors
      } else {
        BitChordLightColors
      }
    } else {
      val resolvedThemeColor = if (albumArtThemeEnabled && dynamicAlbumColor != null) {
        Color(dynamicAlbumColor)
      } else {
        dynamicAlbumColor?.let { Color(it) } ?: themeColor
      }

      val useSystemDynamicColor = (seedPalette == null &&
        shouldUseSystemDynamicColor(
          themeColor = themeColor,
          dynamicAlbumColor = if (albumArtThemeEnabled) dynamicAlbumColor else null,
          liquidGlassEnabled = liquidGlassEnabled,
          bitChordPaletteEnabled = bitChordPaletteEnabled,
          enableDynamicTheme = enableDynamicTheme,
        ) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

      val paletteStyle = paletteStyleFor(seedPalette?.primary ?: resolvedThemeColor)

      val appColorScheme = if (seedPalette != null) {
        exactPaletteColorScheme(
          palette = seedPalette,
          isDark = darkTheme,
        )
      } else {
        materialKolorDynamicColorScheme(
          keyColor = resolvedThemeColor,
          isDark = darkTheme,
          style = paletteStyle,
        )
      }

      val baseColorScheme = if (useSystemDynamicColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      } else {
        appColorScheme
      }

      if (darkTheme && isAmoled) baseColorScheme.pureBlack(true) else baseColorScheme
    }
  }

  val animatedColorScheme = if (disableAnimations) {
    targetColorScheme
  } else {
    animateColorScheme(
      targetColorScheme = targetColorScheme,
      animationSpec = motionScheme.defaultEffectsSpec(),
    )
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
    colorScheme = animatedColorScheme,
    typography = typography,
    shapes = ExpressiveShapes,
    motionScheme = motionScheme,
    content = content
  )
}

private fun Int.toComposeColor(): Color = Color(this.toLong() and 0xFFFFFFFFL)

fun Bitmap.extractThemeColor(): Color {
  val palette =
    Palette
      .from(this)
      .maximumColorCount(16)
      .generate()

  val swatch =
    palette.vibrantSwatch
      ?: palette.dominantSwatch
      ?: palette.mutedSwatch
      ?: palette.lightVibrantSwatch
      ?: palette.darkVibrantSwatch
      ?: palette.lightMutedSwatch
      ?: palette.darkMutedSwatch

  return swatch?.rgb?.toComposeColor() ?: DefaultThemeColor
}

fun Bitmap.extractGradientColors(): List<Color> {
  val palette =
    Palette
      .from(this)
      .maximumColorCount(48)
      .generate()

  val swatches =
    palette.swatches
      .filter { it.population > 0 }
      .sortedByDescending { it.population }

  if (swatches.isEmpty()) {
    return listOf(Color(0xFF595959), Color(0xFF0D0D0D))
  }

  val first = swatches.first()
  val firstHsv = FloatArray(3)
  android.graphics.Color.colorToHSV(first.rgb, firstHsv)

  val second =
    swatches
      .drop(1)
      .maxByOrNull { candidate ->
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(candidate.rgb, hsv)

        val hueDiffRaw = abs(hsv[0] - firstHsv[0])
        val hueDiff = min(hueDiffRaw, 360f - hueDiffRaw) / 180f
        val satDiff = abs(hsv[1] - firstHsv[1])
        val valueDiff = abs(hsv[2] - firstHsv[2])

        hueDiff * 0.65f + satDiff * 0.2f + valueDiff * 0.15f
      }
      ?: first

  return listOf(first.rgb.toComposeColor(), second.rgb.toComposeColor())
    .sortedByDescending { it.luminance() }
}

fun extractWallpaperThemeColor(context: Context): Color? {
  return try {
    val wallpaperManager = WallpaperManager.getInstance(context)
    val drawable = wallpaperManager.drawable ?: return null
    val bitmap =
      if (drawable is BitmapDrawable) {
        drawable.bitmap
      } else {
        val bmp =
          Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
          )
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp
      }
    bitmap.extractThemeColor()
  } catch (e: Exception) {
    null
  }
}

fun ColorScheme.pureBlack(apply: Boolean) =
  if (apply) {
    copy(
      surface = Color.Black,
      background = Color.Black,
    )
  } else {
    this
  }

val ColorSaver =
  object : Saver<Color, Int> {
    override fun restore(value: Int): Color = value.toComposeColor()

    override fun SaverScope.save(value: Color): Int = value.toArgb()
  }

@Composable
fun echomusicTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  pureBlack: Boolean = false,
  dynamicAlbumColor: Int? = null,
  themeColor: Color = DefaultThemeColor,
  seedPalette: ThemeSeedPalette? = null,
  disableAnimations: Boolean = false,
  liquidGlassEnabled: Boolean = false,
  bitChordPaletteEnabled: Boolean = false,
  enableDynamicTheme: Boolean = true,
  appFont: String = "GOOGLE_SANS",
  content: @Composable () -> Unit,
) {
  SpatialFlowTheme(
    darkTheme = darkTheme,
    amoledMode = pureBlack,
    dynamicAlbumColor = if (liquidGlassEnabled || bitChordPaletteEnabled) null else (dynamicAlbumColor ?: (if (themeColor != DefaultThemeColor) themeColor.toArgb() else null)),
    themeColor = themeColor,
    seedPalette = seedPalette,
    disableAnimations = disableAnimations,
    liquidGlassEnabled = liquidGlassEnabled,
    bitChordPaletteEnabled = bitChordPaletteEnabled,
    enableDynamicTheme = enableDynamicTheme,
    appFont = appFont,
    content = content
  )
}

@Serializable
data class ThemeExportV1(
  val version: Int = 1,
  val name: String? = null,
  val primary: String,
  val secondary: String,
  val tertiary: String,
  val neutral: String,
)

object ThemeSeedPaletteCodec {
  private const val PreferencePrefix = "seedPalette:"
  private val json =
    Json {
      prettyPrint = true
      ignoreUnknownKeys = true
      encodeDefaults = true
      isLenient = true
    }

  fun encodeForPreference(
    palette: ThemeSeedPalette,
    name: String? = null,
  ): String {
    val payload =
      json.encodeToString(
        ThemeExportV1(
          name = name,
          primary = palette.primary.toHexArgbString(),
          secondary = palette.secondary.toHexArgbString(),
          tertiary = palette.tertiary.toHexArgbString(),
          neutral = palette.neutral.toHexArgbString(),
        ),
      )
    val b64 = Base64.encodeToString(payload.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
    return PreferencePrefix + b64
  }

  fun decodeFromPreference(value: String): ThemeSeedPalette? {
    if (!value.startsWith(PreferencePrefix)) return null
    val b64 = value.removePrefix(PreferencePrefix)
    val decoded =
      runCatching {
        val bytes = Base64.decode(b64, Base64.URL_SAFE or Base64.NO_WRAP)
        bytes.toString(Charsets.UTF_8)
      }.getOrNull()
        ?: return null
    return decodeFromJson(decoded)
  }

  fun encodeAsJson(
    palette: ThemeSeedPalette,
    name: String? = null,
  ): String =
    json.encodeToString(
      ThemeExportV1(
        name = name,
        primary = palette.primary.toHexArgbString(),
        secondary = palette.secondary.toHexArgbString(),
        tertiary = palette.tertiary.toHexArgbString(),
        neutral = palette.neutral.toHexArgbString(),
      ),
    )

  fun decodeFromJson(text: String): ThemeSeedPalette? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    return runCatching {
      val element = json.parseToJsonElement(trimmed)
      val obj = element.jsonObject

      val version = obj["version"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
      if (version != 1) return@runCatching null

      fun getColor(key: String): Color? =
        obj[key]
          ?.jsonPrimitive
          ?.content
          ?.toColorOrNull()

      val primary = getColor("primary") ?: return@runCatching null
      val secondary = getColor("secondary") ?: primary
      val tertiary = getColor("tertiary") ?: primary
      val neutral = getColor("neutral") ?: primary

      ThemeSeedPalette(
        primary = primary,
        secondary = secondary,
        tertiary = tertiary,
        neutral = neutral,
      )
    }.getOrNull()
      ?: decodeFromLegacyObject(trimmed)
  }

  fun extractNameFromJsonOrNull(text: String): String? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    return runCatching {
      val element: JsonElement = json.parseToJsonElement(trimmed)
      element.jsonObject["name"]
        ?.jsonPrimitive
        ?.content
        ?.takeIf { it.isNotBlank() }
    }.getOrNull()
  }

  fun extractNameFromPreference(value: String): String? {
    if (!value.startsWith(PreferencePrefix)) return null
    val b64 = value.removePrefix(PreferencePrefix)
    val decoded =
      runCatching {
        val bytes = Base64.decode(b64, Base64.URL_SAFE or Base64.NO_WRAP)
        bytes.toString(Charsets.UTF_8)
      }.getOrNull()
        ?: return null
    return extractNameFromJsonOrNull(decoded)
  }

  private fun decodeFromLegacyObject(text: String): ThemeSeedPalette? {
    val trimmed = text.trim()
    if (!trimmed.startsWith("{")) return null
    return runCatching {
      val element = json.parseToJsonElement(trimmed)
      val obj = element.jsonObject

      fun getHex(key: String): String? =
        obj[key]
          ?.jsonPrimitive
          ?.content
          ?.takeIf { it.isNotBlank() }

      val primary = getHex("primary")?.toColorOrNull() ?: return@runCatching null
      val secondary = getHex("secondary")?.toColorOrNull() ?: primary
      val tertiary = getHex("tertiary")?.toColorOrNull() ?: primary
      val neutral = getHex("neutral")?.toColorOrNull() ?: primary

      ThemeSeedPalette(primary, secondary, tertiary, neutral)
    }.getOrNull()
  }

  private fun Color.toHexArgbString(): String = String.format("#%08X", this.toArgb())

  private fun String.toColorOrNull(): Color? {
    val normalized = trim()
    if (normalized.isEmpty()) return null
    return runCatching {
      val withHash = if (normalized.startsWith("#")) normalized else "#$normalized"
      Color(android.graphics.Color.parseColor(withHash))
    }.getOrNull()
  }
}

internal tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}
