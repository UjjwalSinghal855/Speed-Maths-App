package com.ujjwal.speedmath.ui.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ujjwal.speedmath.data.entity.QuestionRecord
import com.ujjwal.speedmath.data.entity.Session
import com.ujjwal.speedmath.data.repository.MathRepository
import com.ujjwal.speedmath.engine.QuestionEngine
import com.ujjwal.speedmath.model.PracticeSettings
import com.ujjwal.speedmath.model.Question
import com.ujjwal.speedmath.utils.StreakManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** UI state emitted to PracticeActivity. */
sealed class PracticeState {
    data class QuestionReady(
        val question: Question,
        val questionNumber: Int,
        val totalQuestions: Int
    ) : PracticeState()

    data class AnswerResult(
        val isCorrect: Boolean,
        val correctAnswer: String
    ) : PracticeState()

    data class TimerTick(
        val elapsedMs: Long,    // time spent on this question
        val countdownMs: Long   // if countdown mode, ms remaining
    ) : PracticeState()

    object SessionComplete : PracticeState()
    object CountdownExpired : PracticeState()
}

class PracticeViewModel(app: Application) : AndroidViewModel(app) {

    val state = MutableLiveData<PracticeState>()

    private lateinit var settings: PracticeSettings
    private lateinit var engine: QuestionEngine
    private val repo = MathRepository(app)
    private val streakManager = StreakManager(app)

    // Session tracking
    private val records = mutableListOf<QuestionRecord>()
    private var currentQuestion: Question? = null
    private var questionStartTime = 0L
    private var questionIndex = 0
    private var sessionStartTime = 0L

    // Timer coroutine job (cancellable)
    private var timerJob: Job? = null

    // ── Initialisation ────────────────────────────────────────────────────────

    fun init(s: PracticeSettings) {
        settings = s
        engine = QuestionEngine(s)
        sessionStartTime = System.currentTimeMillis()
        questionIndex = 0
        records.clear()
        nextQuestion()
    }

    // ── Question flow ─────────────────────────────────────────────────────────

    private fun nextQuestion() {
        if (questionIndex >= settings.questionCount) {
            finishSession()
            return
        }
        questionIndex++
        currentQuestion = engine.nextQuestion()
        questionStartTime = System.currentTimeMillis()

        state.value = PracticeState.QuestionReady(
            question = currentQuestion!!,
            questionNumber = questionIndex,
            totalQuestions = settings.questionCount
        )
        startTimer()
    }

    /** Called from the UI when the user submits an answer. */
    fun submitAnswer(userInput: String) {
        val q = currentQuestion ?: return
        timerJob?.cancel()

        val timeTaken = System.currentTimeMillis() - questionStartTime
        val correct = engine.validateAnswer(userInput, q)

        records.add(
            QuestionRecord(
                sessionId = 0L,           // will be stamped by repository
                questionText = q.text,
                correctAnswer = q.displayAnswer,
                userAnswer = userInput.trim(),
                isCorrect = correct,
                timeTakenMs = timeTaken
            )
        )

        state.value = PracticeState.AnswerResult(correct, q.displayAnswer)
    }

    /** Called after showing feedback; moves to next question. */
    fun advance() = nextQuestion()

    // ── Timer ─────────────────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val countdownMs = if (settings.enableCountdown) settings.countdownSeconds * 1000L else Long.MAX_VALUE
            while (true) {
                delay(100)
                val elapsed = System.currentTimeMillis() - questionStartTime
                val remaining = (countdownMs - elapsed).coerceAtLeast(0L)
                state.value = PracticeState.TimerTick(elapsed, remaining)

                if (settings.enableCountdown && elapsed >= countdownMs) {
                    // Auto-submit a blank when countdown expires
                    submitAnswer("")
                    state.value = PracticeState.CountdownExpired
                    break
                }
            }
        }
    }

    // ── Session completion ────────────────────────────────────────────────────

    private fun finishSession() {
        val totalTime = System.currentTimeMillis() - sessionStartTime
        val correct = records.count { it.isCorrect }
        val total = records.size
        val accuracy = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val avgTime = if (total > 0) totalTime / total else 0L

        val session = Session(
            mode = settings.mode.name,
            totalQuestions = total,
            correctAnswers = correct,
            totalTimeMs = totalTime,
            avgTimePerQuestionMs = avgTime,
            accuracy = accuracy,
            firstRangeMin = settings.firstRangeMin,
            firstRangeMax = settings.firstRangeMax,
            secondRangeMin = settings.secondRangeMin,
            secondRangeMax = settings.secondRangeMax
        )

        viewModelScope.launch {
            repo.saveSession(session, records)
            streakManager.recordPracticeToday()
            state.postValue(PracticeState.SessionComplete)
        }
    }

    // ── Accessors used by ResultActivity ─────────────────────────────────────

    fun getRecords(): List<QuestionRecord> = records.toList()
    fun getSettings(): PracticeSettings = settings
}
