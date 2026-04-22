package com.ujjwal.speedmath.utils

import com.ujjwal.speedmath.model.PracticeMode
import com.ujjwal.speedmath.model.PracticeSettings
import java.util.Calendar

/**
 * Generates a deterministic daily challenge using the current date as a seed.
 * Running this on any device on the same calendar day produces the same challenge.
 * 100% offline – no network required.
 */
object DailyChallengeManager {

    fun getTodayChallenge(): PracticeSettings {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        val seed = year * 1000 + dayOfYear   // unique per day

        // Cycle through modes based on day index (repeats every 9 days)
        val modeIndex = seed % PracticeMode.values().size
        val mode = PracticeMode.values()[modeIndex]

        // Difficulty scales every week
        val weekIndex = (seed / 7) % 3   // 0 = easy, 1 = medium, 2 = hard

        val (fMin, fMax, sMin, sMax) = when (weekIndex) {
            0 -> listOf(1, 9, 1, 9)
            1 -> listOf(10, 99, 1, 9)
            else -> listOf(10, 99, 10, 99)
        }

        return PracticeSettings(
            mode = mode,
            questionCount = 20,
            firstRangeMin = fMin,
            firstRangeMax = fMax,
            secondRangeMin = sMin,
            secondRangeMax = sMax,
            perfectRootsOnly = true,
            enableCountdown = true,
            countdownSeconds = 30
        )
    }
}
