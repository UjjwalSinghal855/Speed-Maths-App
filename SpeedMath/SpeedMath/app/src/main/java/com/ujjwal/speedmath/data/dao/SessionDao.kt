package com.ujjwal.speedmath.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ujjwal.speedmath.data.entity.Session

/** Data Access Object for the [Session] table. */
@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session): Long

    /** Observed by ProgressActivity – auto-updates UI on any DB change. */
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): LiveData<List<Session>>

    /** Last N sessions for chart data (ordered newest-first, reversed in UI). */
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<Session>

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalCount(): Int

    @Query("SELECT AVG(accuracy) FROM sessions")
    suspend fun getOverallAccuracy(): Float?

    @Query("SELECT AVG(avgTimePerQuestionMs) FROM sessions")
    suspend fun getOverallAvgSpeed(): Float?

    /** Best = highest accuracy first, then fastest avg time as tiebreaker. */
    @Query("SELECT * FROM sessions ORDER BY accuracy DESC, avgTimePerQuestionMs ASC LIMIT 1")
    suspend fun getBestSession(): Session?

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC LIMIT 30")
    suspend fun getLast30Sessions(): List<Session>

    /** Used for streak calculation – how many sessions today? */
    @Query("""
        SELECT COUNT(*) FROM sessions 
        WHERE timestamp >= :startOfDayMs AND timestamp < :endOfDayMs
    """)
    suspend fun getSessionCountBetween(startOfDayMs: Long, endOfDayMs: Long): Int

    @Delete
    suspend fun delete(session: Session)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
