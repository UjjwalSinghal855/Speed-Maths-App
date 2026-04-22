package com.ujjwal.speedmath.ui.result

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ujjwal.speedmath.R
import com.ujjwal.speedmath.data.entity.QuestionRecord
import com.ujjwal.speedmath.databinding.ActivityResultBinding
import com.ujjwal.speedmath.databinding.ItemQuestionRecordBinding
import com.ujjwal.speedmath.model.PracticeSettings
import com.ujjwal.speedmath.ui.home.MainActivity
import com.ujjwal.speedmath.ui.progress.ProgressActivity
import com.ujjwal.speedmath.utils.FormatUtils

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val records = (intent.getSerializableExtra(EXTRA_RECORDS) as? ArrayList<*>)
            ?.filterIsInstance<QuestionRecord>() ?: emptyList()
        val settings = intent.getSerializableExtra(EXTRA_SETTINGS) as? PracticeSettings

        showSummary(records)
        setupRecycler(records)
        setupButtons(settings)
    }

    // ── Summary header ────────────────────────────────────────────────────────

    private fun showSummary(records: List<QuestionRecord>) {
        val total   = records.size
        val correct = records.count { it.isCorrect }
        val wrong   = total - correct
        val accuracy = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val totalMs = records.sumOf { it.timeTakenMs }
        val avgMs   = if (total > 0) totalMs / total else 0L

        binding.tvTotalQ.text    = total.toString()
        binding.tvCorrect.text   = correct.toString()
        binding.tvWrong.text     = wrong.toString()
        binding.tvAccuracy.text  = FormatUtils.formatAccuracy(accuracy)
        binding.tvTotalTime.text = FormatUtils.formatTime(totalMs)
        binding.tvAvgTime.text   = FormatUtils.formatTimeShort(avgMs)

        // Colour accuracy ring
        val color = when {
            accuracy >= 90 -> getColor(R.color.correct_green)
            accuracy >= 60 -> getColor(R.color.accent_orange)
            else           -> getColor(R.color.error_red)
        }
        binding.tvAccuracy.setTextColor(color)

        // Performance message
        binding.tvPerformance.text = when {
            accuracy == 100f          -> "🏆 Perfect Score!"
            accuracy >= 90            -> "⭐ Excellent!"
            accuracy >= 75            -> "👍 Good Work!"
            accuracy >= 50            -> "📚 Keep Practicing"
            else                      -> "💪 Don't Give Up!"
        }
    }

    // ── Question list ─────────────────────────────────────────────────────────

    private fun setupRecycler(records: List<QuestionRecord>) {
        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(this@ResultActivity)
            adapter = QuestionReviewAdapter(records)
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun setupButtons(settings: PracticeSettings?) {
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        binding.btnProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.btnPracticeAgain.setOnClickListener {
            // Re-launch with same settings
            if (settings != null) {
                startActivity(Intent(this, com.ujjwal.speedmath.ui.practice.PracticeActivity::class.java)
                    .putExtra(com.ujjwal.speedmath.ui.practice.PracticeActivity.EXTRA_SETTINGS, settings))
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_RECORDS  = "extra_records"
        const val EXTRA_SETTINGS = "extra_settings"
    }

    // ── Inner adapter ──────────────────────────────────────────────────────────

    inner class QuestionReviewAdapter(private val items: List<QuestionRecord>) :
        RecyclerView.Adapter<QuestionReviewAdapter.VH>() {

        inner class VH(val b: ItemQuestionRecordBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            ItemQuestionRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = items[position]
            with(holder.b) {
                tvNumber.text = "#${position + 1}"
                tvQuestion.text = r.questionText
                tvUserAnswer.text = "You: ${r.userAnswer.ifEmpty { "—" }}"
                tvCorrectAnswer.text = "Ans: ${r.correctAnswer}"
                tvTime.text = FormatUtils.formatTimeShort(r.timeTakenMs)

                val bg = if (r.isCorrect) R.color.correct_bg else R.color.error_bg
                root.setCardBackgroundColor(root.context.getColor(bg))
                tvResultIcon.text = if (r.isCorrect) "✓" else "✗"
                tvResultIcon.setTextColor(
                    root.context.getColor(if (r.isCorrect) R.color.correct_green else R.color.error_red)
                )
            }
        }
    }
}
