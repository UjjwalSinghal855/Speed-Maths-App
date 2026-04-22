package com.ujjwal.speedmath.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity recording the result of a single question within a session.
 * Foreign key to [Session] with CASCADE DELETE so cleaning sessions also cleans records.
 */
@Entity(
    tableName = "question_records",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class QuestionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: Long,          // FK → sessions.id
    val questionText: String,     // e.g. "23 + 45"
    val correctAnswer: String,    // e.g. "68"
    val userAnswer: String,       // what the user typed
    val isCorrect: Boolean,
    val timeTakenMs: Long         // milliseconds to answer
)
