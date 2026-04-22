package com.ujjwal.speedmath.data.dao

import androidx.room.*
import com.ujjwal.speedmath.data.entity.QuestionRecord

/** Data Access Object for [QuestionRecord]. */
@Dao
interface QuestionRecordDao {

    @Insert
    suspend fun insertAll(records: List<QuestionRecord>)

    @Query("SELECT * FROM question_records WHERE sessionId = :sessionId")
    suspend fun getForSession(sessionId: Long): List<QuestionRecord>

    /** Lifetime totals across all sessions. */
    @Query("SELECT COUNT(*) FROM question_records WHERE isCorrect = 1")
    suspend fun getTotalCorrect(): Int

    @Query("SELECT COUNT(*) FROM question_records")
    suspend fun getTotalAttempted(): Int
}
