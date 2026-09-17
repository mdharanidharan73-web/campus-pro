package com.example.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Standard Apple iOS Haptic Feedback engine for CampuPro.
 * Translates UI interactions (taps, selections, navigation, gestures)
 * into authentic Apple-like Taptic Engine feedback.
 */
enum class IOSHapticStyle {
    SELECTION,         // Light mechanical tick: tab switches, segmented control slider, filter pills
    LIGHT_IMPACT,      // Subtle impact: list item tap, row expansion, navigation back chevron
    MEDIUM_IMPACT,     // Standard button press: primary buttons, floating controls, toggles
    HEAVY_IMPACT,      // Deep impact: modal sheet presentation, dismiss, delete confirmation
    SUCCESS,           // Two distinct crisp pulses: assignment submission, attendance recorded
    WARNING,           // Cautionary double pulse
    ERROR              // Triple rapid alert pulse
}

class IOSHaptics(
    private val context: Context,
    private val view: View?
) {
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    fun play(style: IOSHapticStyle) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    when (style) {
                        IOSHapticStyle.SELECTION -> {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                            return
                        }
                        IOSHapticStyle.LIGHT_IMPACT -> {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                            return
                        }
                        IOSHapticStyle.MEDIUM_IMPACT -> {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                            return
                        }
                        IOSHapticStyle.HEAVY_IMPACT -> {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                            return
                        }
                        IOSHapticStyle.SUCCESS -> {
                            val timings = longArrayOf(0, 16, 65, 22)
                            val amplitudes = intArrayOf(0, 130, 0, 220)
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                            return
                        }
                        IOSHapticStyle.WARNING -> {
                            val timings = longArrayOf(0, 22, 80, 24)
                            val amplitudes = intArrayOf(0, 180, 0, 180)
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                            return
                        }
                        IOSHapticStyle.ERROR -> {
                            val timings = longArrayOf(0, 20, 50, 20, 50, 32)
                            val amplitudes = intArrayOf(0, 200, 0, 200, 0, 255)
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                            return
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = when (style) {
                        IOSHapticStyle.SELECTION -> 8L
                        IOSHapticStyle.LIGHT_IMPACT -> 14L
                        IOSHapticStyle.MEDIUM_IMPACT -> 24L
                        IOSHapticStyle.HEAVY_IMPACT -> 38L
                        IOSHapticStyle.SUCCESS -> 20L
                        IOSHapticStyle.WARNING -> 28L
                        IOSHapticStyle.ERROR -> 38L
                    }
                    val amplitude = when (style) {
                        IOSHapticStyle.SELECTION -> 50
                        IOSHapticStyle.LIGHT_IMPACT -> 110
                        IOSHapticStyle.MEDIUM_IMPACT -> 180
                        IOSHapticStyle.HEAVY_IMPACT -> 255
                        IOSHapticStyle.SUCCESS -> 190
                        IOSHapticStyle.WARNING -> 210
                        IOSHapticStyle.ERROR -> 255
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                    return
                }
            }

            // Fallback using View Haptic Feedback
            view?.let { v ->
                val constant = when (style) {
                    IOSHapticStyle.SELECTION -> HapticFeedbackConstants.KEYBOARD_TAP
                    IOSHapticStyle.LIGHT_IMPACT -> HapticFeedbackConstants.VIRTUAL_KEY
                    IOSHapticStyle.MEDIUM_IMPACT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) HapticFeedbackConstants.CONTEXT_CLICK
                        else HapticFeedbackConstants.VIRTUAL_KEY
                    }
                    IOSHapticStyle.HEAVY_IMPACT -> HapticFeedbackConstants.LONG_PRESS
                    IOSHapticStyle.SUCCESS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                        else HapticFeedbackConstants.CONTEXT_CLICK
                    }
                    IOSHapticStyle.WARNING, IOSHapticStyle.ERROR -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
                        else HapticFeedbackConstants.LONG_PRESS
                    }
                }
                v.performHapticFeedback(constant)
            }
        } catch (_: Throwable) {
            // Graceful fallback without crashing
        }
    }

    /** Convenient semantic triggers */
    fun selection() = play(IOSHapticStyle.SELECTION)
    fun lightImpact() = play(IOSHapticStyle.LIGHT_IMPACT)
    fun mediumImpact() = play(IOSHapticStyle.MEDIUM_IMPACT)
    fun heavyImpact() = play(IOSHapticStyle.HEAVY_IMPACT)
    fun success() = play(IOSHapticStyle.SUCCESS)
    fun warning() = play(IOSHapticStyle.WARNING)
    fun error() = play(IOSHapticStyle.ERROR)
    fun navigation() = play(IOSHapticStyle.LIGHT_IMPACT)
}

val LocalIOSHaptics = staticCompositionLocalOf<IOSHaptics?> {
    null
}

@Composable
fun rememberIOSHaptics(): IOSHaptics {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view) {
        IOSHaptics(context.applicationContext, view)
    }
}
