package com.ujjwal.speedmath.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tracks the user's consecutive daily practice streak.
 * Data is stored in SharedPreferences (no internet required).
 *
 * Call [recordPracticeToday] once per completed session.
 */
class StreakManager(context: Context) {

    private val prefs = context.getSharedPreferences("speedmath_streak", Context.MODE_PRIVATE)
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun recordPracticeToday() {
        val today = dateFmt.format(Date())
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val current = prefs.getInt(KEY_CURRENT, 0)
        val best = prefs.getInt(KEY_BEST, 0)

        val newStreak = when {
            lastDate == today -> current         // Already practiced today – no change
            isYesterday(lastDate) -> current + 1 // Consecutive day – extend streak
            else -> 1                            // Gap in practice – reset streak
        }

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_CURRENT, newStreak)
            .putInt(KEY_BEST, maxOf(newStreak, best))
            .apply()
    }

    fun getCurrentStreak(): Int = prefs.getInt(KEY_CURRENT, 0)
    fun getBestStreak(): Int = prefs.getInt(KEY_BEST, 0)
    fun getLastPracticeDate(): String = prefs.getString(KEY_LAST_DATE, "Never") ?: "Never"

    /** Returns true if [dateStr] is yesterday's date. */
    private fun isYesterday(dateStr: String?): Boolean {
        dateStr ?: return false
        return try {
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
            dateStr == dateFmt.format(yesterday)
        } catch (_: Exception) { false }
    }

    companion object {
        private const val KEY_LAST_DATE = "last_practice_date"
        private const val KEY_CURRENT = "current_streak"
        private const val KEY_BEST = "best_streak"
    }
}
