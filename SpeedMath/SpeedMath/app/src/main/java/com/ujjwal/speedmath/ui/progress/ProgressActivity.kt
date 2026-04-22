package com.ujjwal.speedmath.ui.progress

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ujjwal.speedmath.R
import com.ujjwal.speedmath.data.entity.Session
import com.ujjwal.speedmath.databinding.ActivityProgressBinding
import com.ujjwal.speedmath.model.PracticeMode
import com.ujjwal.speedmath.utils.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private val viewModel: ProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCharts()
        observeData()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnClearData.setOnClickListener { confirmClear() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun observeData() {
        viewModel.stats.observe(this) { s -> populateStats(s) }
    }

    // ── Stats cards ───────────────────────────────────────────────────────────

    private fun populateStats(s: ProgressStats) {
        binding.tvTotalSessions.text = s.totalSessions.toString()
        binding.tvOverallAccuracy.text = FormatUtils.formatAccuracy(s.overallAccuracy)
        binding.tvAvgSpeed.text = FormatUtils.formatTimeShort(s.avgSpeedMs.toLong())
        binding.tvCurrentStreak.text = "${s.currentStreak} days 🔥"
        binding.tvBestStreak.text = "${s.bestStreak} days"

        s.bestSession?.let { b ->
            binding.tvBestSession.text =
                "${b.mode.replace("_", " ")} • ${FormatUtils.formatAccuracy(b.accuracy)} • ${FormatUtils.formatTimeShort(b.avgTimePerQuestionMs)}"
        } ?: run { binding.tvBestSession.text = "—" }

        // Build charts with recent sessions
        buildAccuracyChart(s.recentSessions)
        buildSpeedChart(s.recentSessions)
    }

    // ── Charts ────────────────────────────────────────────────────────────────

    private fun setupCharts() {
        val charts = listOf(binding.chartAccuracy, binding.chartSpeed)
        charts.forEach { chart ->
            chart.apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                axisRight.isEnabled = false
                legend.isEnabled = true
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = 0xFFE0E0E0.toInt()
                animateX(600)
            }
        }
    }

    private fun buildAccuracyChart(sessions: List<Session>) {
        if (sessions.isEmpty()) return
        val entries = sessions.mapIndexed { i, s -> Entry(i.toFloat(), s.accuracy) }
        val labels = sessions.map { shortDate(it.timestamp) }

        val ds = LineDataSet(entries, "Accuracy %").apply {
            color = getColor(R.color.primary_blue)
            setCircleColor(getColor(R.color.primary_blue))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartAccuracy.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = -30f
            axisLeft.apply { axisMinimum = 0f; axisMaximum = 100f }
            data = LineData(ds)
            invalidate()
        }
    }

    private fun buildSpeedChart(sessions: List<Session>) {
        if (sessions.isEmpty()) return
        val entries = sessions.mapIndexed { i, s ->
            Entry(i.toFloat(), FormatUtils.msToSeconds(s.avgTimePerQuestionMs))
        }
        val labels = sessions.map { shortDate(it.timestamp) }

        val ds = LineDataSet(entries, "Avg Speed (s/question)").apply {
            color = getColor(R.color.accent_orange)
            setCircleColor(getColor(R.color.accent_orange))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartSpeed.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = -30f
            axisLeft.axisMinimum = 0f
            data = LineData(ds)
            invalidate()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun shortDate(ts: Long): String =
        SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(ts))

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all session history and stats. Continue?")
            .setPositiveButton("Delete") { _, _ -> viewModel.clearAll() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
