package com.ujjwal.speedmath.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ujjwal.speedmath.R
import com.ujjwal.speedmath.databinding.ActivityMainBinding
import com.ujjwal.speedmath.model.PracticeMode
import com.ujjwal.speedmath.ui.progress.ProgressActivity
import com.ujjwal.speedmath.ui.settings.SettingsActivity
import com.ujjwal.speedmath.utils.DailyChallengeManager
import com.ujjwal.speedmath.utils.FormatUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupModeCards()
        setupObservers()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.stats.observe(this) { s ->
            binding.tvTotalSessions.text   = s.totalSessions.toString()
            binding.tvCurrentStreak.text   = "${s.currentStreak} 🔥"
            binding.tvBestStreak.text      = "Best: ${s.bestStreak}"
            binding.tvOverallAccuracy.text = FormatUtils.formatAccuracy(s.overallAccuracy)
        }
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.btnViewProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.btnDailyChallenge.setOnClickListener {
            val settings = DailyChallengeManager.getTodayChallenge()
            startActivity(
                Intent(this, SettingsActivity::class.java)
                    .putExtra(SettingsActivity.EXTRA_PRESET, settings)
            )
        }
    }

    /**
     * Populates all 9 mode cards and attaches click listeners.
     * Each <include> root is a CardView; we find its child TextViews by ID to
     * set the icon symbol and label, then route clicks to SettingsActivity.
     */
    private fun setupModeCards() {
        val cards = listOf(
            binding.cardAddition       to PracticeMode.ADDITION,
            binding.cardSubtraction    to PracticeMode.SUBTRACTION,
            binding.cardMultiplication to PracticeMode.MULTIPLICATION,
            binding.cardDivision       to PracticeMode.DIVISION,
            binding.cardMixed          to PracticeMode.MIXED,
            binding.cardSquares        to PracticeMode.SQUARES,
            binding.cardSquareRoots    to PracticeMode.SQUARE_ROOTS,
            binding.cardCubes          to PracticeMode.CUBES,
            binding.cardCubeRoots      to PracticeMode.CUBE_ROOTS
        )

        cards.forEach { (cardView, mode) ->
            // Populate icon and label inside the included item_mode_card layout
            cardView.findViewById<TextView>(R.id.tvModeIcon)?.text = mode.icon
            cardView.findViewById<TextView>(R.id.tvModeName)?.text = mode.displayName
            cardView.setOnClickListener {
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                        .putExtra(SettingsActivity.EXTRA_MODE, mode.name)
                )
            }
        }
    }
}
