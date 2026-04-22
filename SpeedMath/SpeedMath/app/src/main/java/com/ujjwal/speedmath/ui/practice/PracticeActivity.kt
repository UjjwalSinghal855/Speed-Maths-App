package com.ujjwal.speedmath.ui.practice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ujjwal.speedmath.R
import com.ujjwal.speedmath.databinding.ActivityPracticeBinding
import com.ujjwal.speedmath.model.PracticeSettings
import com.ujjwal.speedmath.ui.result.ResultActivity
import com.ujjwal.speedmath.utils.FormatUtils
import com.ujjwal.speedmath.utils.SoundUtils

class PracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeBinding
    private val viewModel: PracticeViewModel by viewModels()
    private var inFeedback = false     // guard to prevent double-submit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val settings = intent.getSerializableExtra(EXTRA_SETTINGS) as? PracticeSettings
            ?: run { finish(); return }

        observeState()

        // Only init on first creation (not rotation)
        if (savedInstanceState == null) viewModel.init(settings)

        setupInput()
    }

    // ── Input wiring ──────────────────────────────────────────────────────────

    private fun setupInput() {
        // Submit on keyboard "Done" action
        binding.etAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { submitAnswer(); true } else false
        }
        binding.btnSubmit.setOnClickListener { submitAnswer() }
        binding.btnNext.setOnClickListener   { advanceToNext() }
    }

    private fun submitAnswer() {
        if (inFeedback) return
        val input = binding.etAnswer.text.toString().trim()
        if (input.isEmpty()) {
            binding.etAnswer.error = "Enter your answer"
            return
        }
        hideKeyboard()
        viewModel.submitAnswer(input)
    }

    private fun advanceToNext() {
        inFeedback = false
        binding.btnNext.visibility = View.GONE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.tvFeedback.visibility = View.GONE
        binding.tvCorrectAnswer.visibility = View.GONE
        binding.etAnswer.text?.clear()
        binding.etAnswer.isEnabled = true
        binding.cardQuestion.setCardBackgroundColor(getColor(R.color.card_default))
        viewModel.advance()
        showKeyboard()
    }

    // ── State observer ────────────────────────────────────────────────────────

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is PracticeState.QuestionReady -> showQuestion(state)
                is PracticeState.TimerTick     -> updateTimer(state)
                is PracticeState.AnswerResult  -> showFeedback(state)
                is PracticeState.CountdownExpired -> {
                    // The timer expired; feedback will follow from AnswerResult
                }
                is PracticeState.SessionComplete -> launchResults()
            }
        }
    }

    // ── UI updates ────────────────────────────────────────────────────────────

    private fun showQuestion(s: PracticeState.QuestionReady) {
        binding.tvQuestion.text = s.question.text
        binding.tvProgress.text = "${s.questionNumber} / ${s.totalQuestions}"
        binding.progressBar.apply {
            max = s.totalQuestions
            progress = s.questionNumber
        }
        binding.tvElapsed.text = "0.0s"
        // Reset to normal countdown bar if enabled
        binding.progressCountdown.visibility =
            if (viewModel.getSettings().enableCountdown) View.VISIBLE else View.GONE
    }

    private fun updateTimer(t: PracticeState.TimerTick) {
        binding.tvElapsed.text = FormatUtils.formatTimeShort(t.elapsedMs)

        if (viewModel.getSettings().enableCountdown) {
            val totalMs = viewModel.getSettings().countdownSeconds * 1000L
            binding.progressCountdown.apply {
                max = totalMs.toInt()
                progress = t.countdownMs.toInt()
            }
            // Turn countdown bar red when < 30% remaining
            if (t.countdownMs.toFloat() / totalMs < 0.3f) {
                binding.progressCountdown.progressTintList =
                    android.content.res.ColorStateList.valueOf(getColor(R.color.error_red))
            }
        }
    }

    private fun showFeedback(result: PracticeState.AnswerResult) {
        inFeedback = true
        if (result.isCorrect) {
            SoundUtils.playCorrect(this)
            binding.tvFeedback.apply {
                text = "✓ Correct!"
                setTextColor(getColor(R.color.correct_green))
                visibility = View.VISIBLE
            }
            binding.cardQuestion.setCardBackgroundColor(getColor(R.color.correct_bg))
        } else {
            SoundUtils.playIncorrect(this)
            binding.tvFeedback.apply {
                text = "✗ Wrong"
                setTextColor(getColor(R.color.error_red))
                visibility = View.VISIBLE
            }
            binding.tvCorrectAnswer.apply {
                text = "Answer: ${result.correctAnswer}"
                visibility = View.VISIBLE
            }
            binding.cardQuestion.setCardBackgroundColor(getColor(R.color.error_bg))
        }
        binding.etAnswer.isEnabled = false
        binding.btnSubmit.visibility = View.GONE
        binding.btnNext.visibility = View.VISIBLE
    }

    private fun launchResults() {
        SoundUtils.playSessionComplete(this)
        startActivity(
            Intent(this, ResultActivity::class.java)
                .putExtra(ResultActivity.EXTRA_RECORDS,  ArrayList(viewModel.getRecords()))
                .putExtra(ResultActivity.EXTRA_SETTINGS, viewModel.getSettings())
        )
        finish()
    }

    // ── Keyboard helpers ──────────────────────────────────────────────────────

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etAnswer.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.etAnswer.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etAnswer, InputMethodManager.SHOW_IMPLICIT)
    }

    companion object {
        const val EXTRA_SETTINGS = "extra_settings"
    }
}
