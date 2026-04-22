package com.ujjwal.speedmath.utils

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Provides sound and vibration feedback for correct/incorrect answers.
 * Uses the device's system sounds (no extra assets needed → stays lightweight).
 */
object SoundUtils {

    /** Short "tick" for correct answer. */
    fun playCorrect(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1f)
        vibrate(context, 60L)
    }

    /** Double buzz for wrong answer. */
    fun playIncorrect(context: Context) {
        vibrate(context, 120L)
    }

    /** Celebratory triple buzz on session complete. */
    fun playSessionComplete(context: Context) {
        vibrate(context, longArrayOf(0, 80, 60, 80, 60, 120))
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun vibrate(context: Context, duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(duration)
                }
            }
        } catch (_: Exception) { /* Vibrator not available on emulator – ignore */ }
    }

    private fun vibrate(context: Context, pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) { }
    }
}
