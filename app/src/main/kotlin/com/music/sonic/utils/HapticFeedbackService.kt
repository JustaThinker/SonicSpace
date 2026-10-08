package com.music.sonic.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Universal Haptic Feedback Service for SonicSpace.
 *
 * Provides responsive, low-latency haptic feedback across all Android versions
 * and hardware types (ERM, linear resonant actuators, etc.) with customizable intensity.
 */
class HapticFeedbackService(
    private val context: Context,
    private val view: View? = null,
    var isEnabled: Boolean = false,
    var strength: Float = 80f // 0f .. 100f
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val hasVibrator: Boolean by lazy {
        vibrator?.hasVibrator() == true
    }

    private val hasAmplitudeControl: Boolean by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasAmplitudeControl() == true
    }

    private fun calculateAmplitude(relativeFraction: Float = 1.0f): Int {
        val normalized = (strength / 100f).coerceIn(0.05f, 1.0f) * relativeFraction
        return (normalized * 255).toInt().coerceIn(1, 255)
    }

    /**
     * Crisp click feedback for button taps, navigation items, chips, and list selections.
     */
    fun performClick(force: Boolean = false) {
        if (!isEnabled && !force) return

        if (hasVibrator && vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hasAmplitudeControl) {
                try {
                    val amplitude = calculateAmplitude(0.75f)
                    val effect = VibrationEffect.createOneShot(18, amplitude)
                    vibrateEffect(effect)
                    return
                } catch (_: Exception) {}
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val amplitude = calculateAmplitude(0.75f)
                    val effect = VibrationEffect.createOneShot(22, amplitude)
                    vibrateEffect(effect)
                    return
                } catch (_: Exception) {}
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(22)
                return
            }
        }

        // Fallback to View haptic feedback
        view?.let { v ->
            v.isHapticFeedbackEnabled = true
            v.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    /**
     * Subtle tick feedback for slider scrubbing, drag handles, and boundaries.
     */
    fun performTick(force: Boolean = false) {
        if (!isEnabled && !force) return

        if (hasVibrator && vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hasAmplitudeControl) {
                try {
                    val amplitude = calculateAmplitude(0.40f)
                    val effect = VibrationEffect.createOneShot(8, amplitude)
                    vibrateEffect(effect)
                    return
                } catch (_: Exception) {}
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val amplitude = calculateAmplitude(0.40f)
                    val effect = VibrationEffect.createOneShot(10, amplitude)
                    vibrateEffect(effect)
                    return
                } catch (_: Exception) {}
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
                return
            }
        }

        view?.let { v ->
            v.isHapticFeedbackEnabled = true
            v.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    /**
     * Scroll tick with adaptive throttle and light intensity.
     */
    fun performScroll(force: Boolean = false) {
        if (!isEnabled && !force) return
        performTick(force = force)
    }

    /**
     * Toggle feedback pattern (rising for ON, falling for OFF).
     */
    fun performToggle(isOn: Boolean, force: Boolean = false) {
        if (!isEnabled && !force) return

        if (hasVibrator && vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasAmplitudeControl) {
            try {
                val timings = if (isOn) longArrayOf(0, 10, 15, 18) else longArrayOf(0, 18, 15, 10)
                val ampLow = calculateAmplitude(0.4f)
                val ampHigh = calculateAmplitude(0.9f)
                val amplitudes = if (isOn) intArrayOf(0, ampLow, 0, ampHigh) else intArrayOf(0, ampHigh, 0, ampLow)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrateEffect(effect)
                return
            } catch (_: Exception) {}
        }
        performClick(force = force)
    }

    /**
     * Long press tactile response.
     */
    fun performLongPress(force: Boolean = false) {
        if (!isEnabled && !force) return

        if (hasVibrator && vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val amplitude = calculateAmplitude(1.0f)
                val effect = VibrationEffect.createOneShot(45, amplitude)
                vibrateEffect(effect)
                return
            } catch (_: Exception) {}
        }

        view?.let { v ->
            v.isHapticFeedbackEnabled = true
            v.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    /**
     * Custom preview vibration for testing slider adjustments in settings.
     */
    fun previewVibration(strengthVal: Float) {
        val oldEnabled = isEnabled
        val oldStrength = strength
        isEnabled = true
        strength = strengthVal
        try {
            performClick(force = true)
        } finally {
            isEnabled = oldEnabled
            strength = oldStrength
        }
    }

    private fun vibrateEffect(effect: VibrationEffect) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build()
            v.vibrate(effect, attributes)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            @Suppress("DEPRECATION")
            v.vibrate(effect, attributes)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(effect)
        }
    }
}

val LocalHapticFeedbackService = compositionLocalOf<HapticFeedbackService> {
    error("No HapticFeedbackService provided")
}

@Composable
fun rememberHapticFeedbackService(
    isEnabled: Boolean = true,
    strength: Float = 80f
): HapticFeedbackService {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view) {
        HapticFeedbackService(
            context = context.applicationContext,
            view = view,
            isEnabled = isEnabled,
            strength = strength
        )
    }.apply {
        this.isEnabled = isEnabled
        this.strength = strength
    }
}
