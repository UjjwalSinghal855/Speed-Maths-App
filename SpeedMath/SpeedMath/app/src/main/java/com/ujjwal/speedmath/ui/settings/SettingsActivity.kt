package com.ujjwal.speedmath.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ujjwal.speedmath.databinding.ActivitySettingsBinding
import com.ujjwal.speedmath.model.PracticeMode
import com.ujjwal.speedmath.model.PracticeSettings
import com.ujjwal.speedmath.ui.practice.PracticeActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var selectedMode: PracticeMode = PracticeMode.ADDITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill from extras (mode card tap OR daily challenge preset)
        val modeName = intent.getStringExtra(EXTRA_MODE)
        if (modeName != null) {
            selectedMode = PracticeMode.valueOf(modeName)
        }

        @Suppress("DEPRECATION")
        val preset = intent.getSerializableExtra(EXTRA_PRESET) as? PracticeSettings
        if (preset != null) {
            applyPreset(preset)
            return  // Skip further setup – preset is fully loaded
        }

        setupUI()
    }

    private fun setupUI() {
        // Show which mode is selected
        binding.tvSelectedMode.text = selectedMode.displayName

        // Default range values
        binding.etFirstMin.setText("1")
        binding.etFirstMax.setText("99")
        binding.etSecondMin.setText("1")
        binding.etSecondMax.setText("99")
        binding.etCountdown.setText("30")

        // Hide second-range row for single-operand modes
        updateSecondRangeVisibility()

        // Mode spinner / re-select
        binding.spinnerMode.apply {
            val modes = PracticeMode.values()
            val names = modes.map { it.displayName }.toTypedArray()
            adapter = android.widget.ArrayAdapter(
                this@SettingsActivity,
                android.R.layout.simple_spinner_item,
                names
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            setSelection(selectedMode.ordinal)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                    selectedMode = modes[pos]
                    updateSecondRangeVisibility()
                }
                override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
            }
        }

        // Quick question-count buttons
        binding.btn10.setOnClickListener  { binding.etQuestionCount.setText("10")  }
        binding.btn20.setOnClickListener  { binding.etQuestionCount.setText("20")  }
        binding.btn30.setOnClickListener  { binding.etQuestionCount.setText("30")  }
        binding.btn50.setOnClickListener  { binding.etQuestionCount.setText("50")  }
        binding.etQuestionCount.setText("20")

        // Quick range presets
        binding.btnRange1d.setOnClickListener { setRange(1, 9, 1, 9) }
        binding.btnRange2d.setOnClickListener { setRange(10, 99, 10, 99) }
        binding.btnRange3d.setOnClickListener { setRange(100, 999, 100, 999) }

        binding.switchCountdown.setOnCheckedChangeListener { _, checked ->
            binding.etCountdown.isEnabled = checked
        }

        binding.btnStartPractice.setOnClickListener { validateAndStart() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun updateSecondRangeVisibility() {
        val singleOperand = selectedMode in listOf(
            PracticeMode.SQUARES, PracticeMode.CUBES,
            PracticeMode.SQUARE_ROOTS, PracticeMode.CUBE_ROOTS
        )
        val vis = if (singleOperand) android.view.View.GONE else android.view.View.VISIBLE
        binding.layoutSecondRange.visibility = vis
    }

    private fun setRange(f1: Int, f2: Int, s1: Int, s2: Int) {
        binding.etFirstMin.setText(f1.toString())
        binding.etFirstMax.setText(f2.toString())
        binding.etSecondMin.setText(s1.toString())
        binding.etSecondMax.setText(s2.toString())
    }

    private fun validateAndStart() {
        try {
            val qCount = binding.etQuestionCount.text.toString().toInt()
            if (qCount < 1 || qCount > 200) { toast("Question count: 1–200"); return }

            val f1 = binding.etFirstMin.text.toString().toInt()
            val f2 = binding.etFirstMax.text.toString().toInt()
            val s1 = binding.etSecondMin.text.toString().toInt()
            val s2 = binding.etSecondMax.text.toString().toInt()

            if (f1 > f2) { toast("First range: min ≤ max"); return }
            if (s1 > s2) { toast("Second range: min ≤ max"); return }
            if (f1 < 0 || s1 < 0) { toast("Ranges must be ≥ 0"); return }

            val cdEnabled = binding.switchCountdown.isChecked
            val cdSecs = if (cdEnabled) {
                val v = binding.etCountdown.text.toString().toIntOrNull() ?: 30
                v.coerceIn(5, 300)
            } else 30

            val settings = PracticeSettings(
                mode = selectedMode,
                questionCount = qCount,
                firstRangeMin = f1, firstRangeMax = f2,
                secondRangeMin = s1, secondRangeMax = s2,
                perfectRootsOnly = binding.switchPerfectRoots.isChecked,
                enableCountdown = cdEnabled,
                countdownSeconds = cdSecs
            )
            startActivity(
                Intent(this, PracticeActivity::class.java)
                    .putExtra(PracticeActivity.EXTRA_SETTINGS, settings)
            )
        } catch (_: NumberFormatException) {
            toast("Please fill all fields with valid numbers")
        }
    }

    /** Populates UI from a pre-built [PracticeSettings] (used by Daily Challenge). */
    private fun applyPreset(p: PracticeSettings) {
        setupUI()
        selectedMode = p.mode
        binding.spinnerMode.setSelection(p.mode.ordinal)
        binding.etQuestionCount.setText(p.questionCount.toString())
        setRange(p.firstRangeMin, p.firstRangeMax, p.secondRangeMin, p.secondRangeMax)
        binding.switchCountdown.isChecked = p.enableCountdown
        binding.etCountdown.setText(p.countdownSeconds.toString())
        binding.switchPerfectRoots.isChecked = p.perfectRootsOnly
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_MODE   = "extra_mode"
        const val EXTRA_PRESET = "extra_preset"
    }
}
