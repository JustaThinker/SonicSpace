/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.sonic.ui.component

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.sonic.ui.component.backdrop.Backdrop
import com.music.sonic.ui.component.backdrop.drawBackdrop
import com.music.sonic.ui.component.backdrop.effects.blur
import com.music.sonic.ui.component.backdrop.effects.colorControls
import com.music.sonic.ui.component.backdrop.effects.lens
import com.music.sonic.ui.component.backdrop.highlight.Highlight
import com.music.sonic.ui.component.backdrop.shadow.Shadow

/**
 * User-configurable parameters of the liquid glass effect, sourced from DataStore
 * preferences in [com.music.sonic.MainActivity] and distributed through
 * [LocalGlassEffectConfig].
 */
@Stable
data class GlassEffectConfig(
    val globalEnabled: Boolean = false,
    val vibrancy: Float = 1.0f,
    /** Blur in dp applied to glass pills. Defaults to 8.dp (BitChord Apple-matched default). */
    val blurRadius: Float = 8f,
    /** 0..1, mapped to 0..[LENS_MAX_DP] dp of lens refraction height. 0.5 = 24dp (BitChord default). */
    val lensHeight: Float = 0.5f,
    /** 0..1, mapped to 0..[LENS_MAX_DP] dp of lens refraction amount. 0.5 = 24dp (BitChord default). */
    val lensAmount: Float = 0.5f,
    val chromaticAberration: Boolean = true,
    val depthEffect: Boolean = true,
    /** [Color.Unspecified] means adaptive: light glass on light theme, dark on dark. */
    val surfaceTintColor: Color = Color.Unspecified,
    val surfaceOpacity: Float = 0.4f,
    val textColor: Color = Color.Unspecified,
    val playerEnabled: Boolean = true,
    val miniPlayerEnabled: Boolean = true,
    val navBarEnabled: Boolean = true,
) {
    /**
     * Whether the glass effect should be rendered for [component], taking the master
     * switch and the per-component switch into account.
     */
    fun isEnabledFor(component: GlassComponent): Boolean =
        globalEnabled && when (component) {
            GlassComponent.PLAYER -> playerEnabled
            GlassComponent.MINI_PLAYER -> miniPlayerEnabled
            GlassComponent.NAV_BAR -> navBarEnabled
        }
}

/**
 * State holding liquid glass styling and configuration.
 */
@Stable
class LiquidGlassState(
    val config: GlassEffectConfig = GlassEffectConfig()
)

@Composable
fun rememberLiquidGlassState(
    config: GlassEffectConfig = LocalGlassEffectConfig.current
): LiquidGlassState = remember(config) { LiquidGlassState(config) }

/**
 * Resolves the configured text color against the current theme when [GlassEffectConfig.textColor]
 * is [Color.Unspecified]. Guaranteed to return a valid specified color.
 */
@Composable
fun GlassEffectConfig.resolveTextColor(): Color =
    if (textColor.isSpecified) {
        textColor
    } else if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

/**
 * Resolves the configured surface tint color against the current theme when
 * [GlassEffectConfig.surfaceTintColor] is [Color.Unspecified].
 */
@Composable
fun GlassEffectConfig.resolveSurfaceTintColor(): Color =
    if (surfaceTintColor.isSpecified) {
        surfaceTintColor
    } else if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF121212)
    }

/** UI surfaces that can individually opt in or out of the liquid glass effect. */
enum class GlassComponent {
    PLAYER,
    MINI_PLAYER,
    NAV_BAR,
}

/**
 * Maximum lens refraction in dp when the 0..1 preference sliders are at 1. The
 * defaults (0.5) land on the 24dp height/amount used by the library author's
 * Apple-matched LiquidBottomTabs recipe.
 */
internal const val LENS_MAX_DP = 48f

/**
 * The full screen player uses a much heavier blur than the glass pills, matching
 * Apple Music where the now playing background is a deep-blurred material while
 * only the small controls are clear liquid glass.
 */
internal const val PLAYER_BLUR_MULTIPLIER = 4f

/** Lowest resolution fraction glass surfaces are rendered at (heavy blur hides it). */
internal const val MIN_GLASS_RESOLUTION_SCALE = 0.33f

/** Blur radius (dp) at or above which the minimum resolution scale is safe to use. */
internal const val FULL_QUALITY_BLUR_DP = 8f

/**
 * Resolution fraction at which a glass surface records and processes its backdrop.
 * Blur masks the upscaling, so the more blur, the lower the resolution can go: at
 * [FULL_QUALITY_BLUR_DP]+ dp of blur the surface renders at
 * [MIN_GLASS_RESOLUTION_SCALE]; with no blur it stays at full resolution so the
 * clear glass center remains crisp.
 */
fun glassResolutionScale(blurRadiusDp: Float): Float {
    val t = (blurRadiusDp / FULL_QUALITY_BLUR_DP).coerceIn(0f, 1f)
    return 1f - t * (1f - MIN_GLASS_RESOLUTION_SCALE)
}

/**
 * The backdrop blur pipeline requires [android.graphics.RenderEffect] on a
 * [android.graphics.RenderNode], which is available from Android 12 (API 31).
 */
fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

/**
 * Maps the user-facing vibrancy preference (0..2, default 1) to a saturation multiplier.
 * A value of 1 matches the library's built-in vibrancy effect (saturation x1.5), 0 leaves
 * colors untouched and 2 doubles the saturation.
 */
fun glassSaturation(vibrancy: Float): Float = 1f + 0.5f * vibrancy.coerceIn(0f, 2f)

val LocalGlassEffectConfig = staticCompositionLocalOf { GlassEffectConfig() }

/** The backdrop content (app UI) that glass surfaces sample from. */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop> { error("No AppBackdrop provided") }

/**
 * Renders this composable as a liquid glass surface sampling [LocalAppBackdrop].
 *
 * Applies the configured vibrancy, blur and lens refraction effects, then draws the
 * surface tint (theme-adaptive unless the user picked a color). Effects whose
 * parameters make them a no-op are skipped entirely to keep the RenderEffect chain
 * as short as possible, and the backdrop is processed at [glassResolutionScale] of
 * the surface resolution. Returns the receiver unchanged on devices without
 * RenderEffect support.
 *
 * [applyEdgeEffects] controls the edge treatment that makes small pills read as
 * physical glass: lens refraction, the specular highlight rim and the drop shadow.
 * It should be false for large surfaces such as the full screen player, where the
 * rim renders as a stray band of light.
 *
 * [blurRadiusDp] overrides the configured blur; the full screen player passes a
 * heavier value ([PLAYER_BLUR_MULTIPLIER]x) than the clear glass pills.
 *
 * [shape] is restricted to [CornerBasedShape] because the backdrop lens effect throws
 * [UnsupportedOperationException] for any other shape type.
 */
@Composable
fun Modifier.liquidGlass(
    state: LiquidGlassState,
    shape: CornerBasedShape = RoundedCornerShape(28.dp),
    applyEdgeEffects: Boolean = true,
    blurRadius: Dp = state.config.blurRadius.dp,
    saturation: Float = glassSaturation(state.config.vibrancy),
    tint: Color = state.config.resolveSurfaceTintColor().copy(alpha = state.config.surfaceOpacity),
    refraction: Float = state.config.lensAmount,
): Modifier = this.liquidGlass(
    config = state.config,
    shape = shape,
    applyEdgeEffects = applyEdgeEffects,
    blurRadiusDp = blurRadius.value,
    saturation = saturation,
    tint = tint,
    refraction = refraction,
)

@Composable
fun Modifier.liquidGlass(
    config: GlassEffectConfig,
    shape: CornerBasedShape = RoundedCornerShape(28.dp),
    applyEdgeEffects: Boolean = true,
    blurRadiusDp: Float = config.blurRadius,
    saturation: Float = glassSaturation(config.vibrancy),
    tint: Color = config.resolveSurfaceTintColor().copy(alpha = config.surfaceOpacity),
    refraction: Float = config.lensAmount,
): Modifier {
    if (!isGlassSupported()) return this
    val backdrop = LocalAppBackdrop.current
    val density = LocalDensity.current
    val resolutionScale = glassResolutionScale(blurRadiusDp)
    // Pixel-sized effect parameters operate on the downscaled backdrop layer, so
    // they are pre-multiplied by the resolution scale to keep the same visual size.
    val blurPx = with(density) { blurRadiusDp.dp.toPx() } * resolutionScale
    val lensHeightPx = with(density) { (refraction * LENS_MAX_DP).dp.toPx() } * resolutionScale
    val lensAmountPx = with(density) { (refraction * LENS_MAX_DP).dp.toPx() } * resolutionScale

    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            if (saturation != 1f) {
                colorControls(saturation = saturation)
            }
            if (blurPx > 0f) {
                blur(blurPx)
            }
            if (applyEdgeEffects &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (lensHeightPx > 0f || lensAmountPx > 0f)
            ) {
                lens(
                    refractionHeight = lensHeightPx,
                    refractionAmount = lensAmountPx,
                    depthEffect = config.depthEffect,
                    chromaticAberration = config.chromaticAberration,
                )
            }
        },
        highlight = if (applyEdgeEffects) ({ Highlight.Default }) else null,
        shadow = if (applyEdgeEffects) ({ Shadow.Default }) else null,
        onDrawSurface = {
            if (tint.alpha > 0f) {
                drawRect(
                    color = tint,
                    size = size,
                )
            }
        },
        backdropScale = resolutionScale,
    )
}

/**
 * Finds the nearest [Window] associated with [view], checking for dialog windows first.
 */
fun findDialogWindow(view: View): Window? {
    var parent = view.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) {
            return parent.window
        }
        parent = parent.parent
    }
    var ctx = view.context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx.window
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Applies native hardware blur behind the window hosting this composable on Android 12+ (API 31+).
 * Safe no-op on earlier Android releases.
 */
@Composable
fun ApplyWindowBlurBehind(blurRadiusPx: Int = 60) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val view = LocalView.current
        DisposableEffect(view, blurRadiusPx) {
            try {
                val window = findDialogWindow(view)
                window?.let { w ->
                    w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    w.attributes = w.attributes.apply {
                        this.blurBehindRadius = blurRadiusPx
                    }
                }
            } catch (_: Throwable) {}
            onDispose {}
        }
    }
}

