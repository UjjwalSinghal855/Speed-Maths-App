package com.ujjwal.speedmath.utils

/**
 * Shared formatting utilities used across multiple screens.
 */
object FormatUtils {

    /** Converts milliseconds → "m:ss" display string, e.g. 75000 → "1:15" */
    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) "$minutes:${seconds.toString().padStart(2, '0')}"
        else "${seconds}s"
    }

    /** Converts milliseconds → "X.Xs" for per-question display, e.g. 4200 → "4.2s" */
    fun formatTimeShort(ms: Long): String {
        val secs = ms / 1000.0
        return String.format("%.1fs", secs)
    }

    /** Converts milliseconds → seconds with one decimal for chart labels */
    fun msToSeconds(ms: Long): Float = ms / 1000f

    /** Formats accuracy to one decimal place with % symbol */
    fun formatAccuracy(pct: Float): String = String.format("%.1f%%", pct)

    /**
     * Computes start-of-day epoch millis for streak queries.
     * Returns pair: (startOfDayMs, endOfDayMs)
     */
    fun todayBounds(): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 24 * 60 * 60 * 1000L
        return start to end
    }
}
