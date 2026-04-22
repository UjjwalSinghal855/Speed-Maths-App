package com.ujjwal.speedmath.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for one completed practice session.
 * Each session has aggregate stats plus the individual [QuestionRecord]s linked by sessionId.
 */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** PracticeMode.name string, e.g. "ADDITION" */
    val mode: String,

    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalTimeMs: Long,
    val avgTimePerQuestionMs: Long,

    /** 0.0 – 100.0 */
    val accuracy: Float,

    /** Unix timestamp millis when the session ended */
    val timestamp: Long = System.currentTimeMillis(),

    // Stored so progress screen can show context for each session
    val firstRangeMin: Int,
    val firstRangeMax: Int,
    val secondRangeMin: Int,
    val secondRangeMax: Int
)
