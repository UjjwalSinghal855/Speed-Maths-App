package com.ujjwal.speedmath.model

import java.io.Serializable

/**
 * All configuration choices the user makes in SettingsActivity.
 * Passed to PracticeActivity as an Intent Serializable extra.
 *
 * For single-operand modes (Squares, Cubes, Roots), secondRangeMin/Max are
 * ignored by the engine.
 */
data class PracticeSettings(
    val mode: PracticeMode,

    /** Number of questions in this session (10 / 20 / 30 / 50 / custom) */
    val questionCount: Int,

    /** Range for the first (or only) operand */
    val firstRangeMin: Int,
    val firstRangeMax: Int,

    /** Range for the second operand (ignored for SQUARES, CUBES, *_ROOTS) */
    val secondRangeMin: Int,
    val secondRangeMax: Int,

    /** If true, √ and ∛ questions always produce whole-number answers */
    val perfectRootsOnly: Boolean = true,

    /** Countdown timer duration in seconds per question (0 = infinite) */
    val countdownSeconds: Int = 30,

    /** Whether countdown mode is enabled */
    val enableCountdown: Boolean = false
) : Serializable
