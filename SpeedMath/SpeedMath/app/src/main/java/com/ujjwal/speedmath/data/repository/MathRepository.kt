package com.ujjwal.speedmath.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ujjwal.speedmath.data.db.AppDatabase
import com.ujjwal.speedmath.data.entity.QuestionRecord
import com.ujjwal.speedmath.data.entity.Session

/**
 * Repository layer that abstracts the Room database from ViewModels.
 * All suspend functions must be called from a coroutine.
 */
class MathRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val sessionDao = db.sessionDao()
    private val recordDao = db.questionRecordDao()

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Saves a completed session AND all its question records atomically.
     * Returns the auto-generated session ID.
     */
    suspend fun saveSession(session: Session, records: List<QuestionRecord>): Long {
        val sessionId = sessionDao.insert(session)
        // Stamp all records with the real session ID before inserting
        val stamped = records.map { it.copy(sessionId = sessionId) }
        recordDao.insertAll(stamped)
        return sessionId
    }

    suspend fun deleteAllSessions() = sessionDao.deleteAll()

    // ── Read (LiveData – observable) ─────────────────────────────────────────

    fun getAllSessions(): LiveData<List<Session>> = sessionDao.getAllSessions()

    // ── Read (suspend – one-shot) ─────────────────────────────────────────────

    suspend fun getLast30Sessions(): List<Session> = sessionDao.getLast30Sessions()

    suspend fun getTotalSessions(): Int = sessionDao.getTotalCount()

    suspend fun getOverallAccuracy(): Float = sessionDao.getOverallAccuracy() ?: 0f

    suspend fun getOverallAvgSpeedMs(): Float = sessionDao.getOverallAvgSpeed() ?: 0f

    suspend fun getBestSession(): Session? = sessionDao.getBestSession()

    suspend fun getRecordsForSession(sessionId: Long): List<QuestionRecord> =
        recordDao.getForSession(sessionId)

    suspend fun getSessionCountBetween(startMs: Long, endMs: Long): Int =
        sessionDao.getSessionCountBetween(startMs, endMs)
}
