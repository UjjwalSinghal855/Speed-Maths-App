package com.ujjwal.speedmath.engine

import com.ujjwal.speedmath.model.PracticeMode
import com.ujjwal.speedmath.model.PracticeSettings
import com.ujjwal.speedmath.model.Question
import kotlin.math.*
import kotlin.random.Random

/**
 * Core question generation engine.
 *
 * Creates randomised math questions according to [PracticeSettings].
 * Tracks used questions within the session to minimise repetition.
 * Also validates user-typed answers.
 */
class QuestionEngine(private val settings: PracticeSettings) {

    private val usedTexts = mutableSetOf<String>()
    private val rng = Random(System.currentTimeMillis())

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Generates the next question. Will attempt up to 200 times to produce a
     * question not already seen in this session (graceful fallback if pool exhausted).
     */
    fun nextQuestion(): Question {
        var q: Question
        var attempts = 0
        do {
            q = generate()
            attempts++
        } while (usedTexts.contains(q.text) && attempts < 200)
        usedTexts.add(q.text)
        return q
    }

    /**
     * Returns true if [userInput] is a correct answer for [question].
     * Handles integer answers (exact) and root answers (within tolerance).
     */
    fun validateAnswer(userInput: String, question: Question): Boolean {
        val trimmed = userInput.trim()
        if (trimmed.isEmpty()) return false
        return try {
            val userVal = trimmed.toDouble()
            if (question.tolerance > 0.0) {
                abs(userVal - question.answer) <= question.tolerance
            } else {
                // Integer comparison: both values must be equal AND be whole numbers
                val ansLong = question.answer.toLong()
                val userLong = userVal.toLong()
                ansLong == userLong && userVal == floor(userVal) && question.answer == floor(question.answer)
            }
        } catch (_: NumberFormatException) {
            false
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun generate(): Question = when (settings.mode) {
        PracticeMode.ADDITION       -> makeAddition()
        PracticeMode.SUBTRACTION    -> makeSubtraction()
        PracticeMode.MULTIPLICATION -> makeMultiplication()
        PracticeMode.DIVISION       -> makeDivision()
        PracticeMode.MIXED          -> makeMixed()
        PracticeMode.SQUARES        -> makeSquare()
        PracticeMode.SQUARE_ROOTS   -> makeSquareRoot()
        PracticeMode.CUBES          -> makeCube()
        PracticeMode.CUBE_ROOTS     -> makeCubeRoot()
    }

    /** Random int in [min, max] inclusive. */
    private fun r1() = rng.nextInt(settings.firstRangeMax - settings.firstRangeMin + 1) + settings.firstRangeMin
    private fun r2() = rng.nextInt(settings.secondRangeMax - settings.secondRangeMin + 1) + settings.secondRangeMin

    // ── Arithmetic ───────────────────────────────────────────────────────────

    private fun makeAddition(): Question {
        val a = r1(); val b = r2()
        return q("$a + $b", (a + b).toDouble())
    }

    private fun makeSubtraction(): Question {
        val x = r1(); val y = r2()
        // Ensure non-negative result by ordering operands
        val (a, b) = if (x >= y) x to y else y to x
        return q("$a − $b", (a - b).toDouble())
    }

    private fun makeMultiplication(): Question {
        val a = r1(); val b = r2()
        val ans = a.toLong() * b
        return q("$a × $b", ans.toDouble())
    }

    /**
     * Division always produces a whole-number quotient.
     * Strategy: pick quotient from first range, divisor from second range,
     * then dividend = quotient × divisor.
     */
    private fun makeDivision(): Question {
        val divisor = max(r2(), 1)          // avoid ÷0
        val quotient = r1()
        val dividend = quotient.toLong() * divisor
        return q("$dividend ÷ $divisor", quotient.toDouble())
    }

    private fun makeMixed(): Question = when (rng.nextInt(4)) {
        0 -> makeAddition()
        1 -> makeSubtraction()
        2 -> makeMultiplication()
        else -> makeDivision()
    }

    // ── Powers and Roots ─────────────────────────────────────────────────────

    private fun makeSquare(): Question {
        val a = r1()
        val ans = a.toLong() * a
        return q("$a²", ans.toDouble())
    }

    private fun makeSquareRoot(): Question {
        return if (settings.perfectRootsOnly) {
            // Root is in first range, build the square from it
            val root = r1()
            val sq = root.toLong() * root
            q("√$sq", root.toDouble())
        } else {
            // Random square in range [min², max²], may not be perfect
            val n = (rng.nextLong(
                (settings.firstRangeMin.toLong() * settings.firstRangeMin).coerceAtLeast(1L),
                (settings.firstRangeMax.toLong() * settings.firstRangeMax) + 1L
            ))
            val ans = sqrt(n.toDouble())
            val isPerfect = ans == floor(ans)
            val display = if (isPerfect) ans.toLong().toString() else String.format("%.2f", ans)
            Question("√$n", if (isPerfect) ans else (ans * 100).roundToInt() / 100.0, display, if (isPerfect) 0.0 else 0.05)
        }
    }

    private fun makeCube(): Question {
        val a = r1()
        val ans = a.toLong() * a * a
        return q("$a³", ans.toDouble())
    }

    private fun makeCubeRoot(): Question {
        return if (settings.perfectRootsOnly) {
            val root = r1()
            val cube = root.toLong() * root * root
            q("∛$cube", root.toDouble())
        } else {
            // For simplicity, cube roots are always perfect in SpeedMath
            val root = r1()
            val cube = root.toLong() * root * root
            q("∛$cube", root.toDouble())
        }
    }

    /** Convenience builder for integer-answer questions. */
    private fun q(text: String, answer: Double) =
        Question(text, answer, formatAnswer(answer))

    private fun formatAnswer(ans: Double): String =
        if (ans == floor(ans)) ans.toLong().toString() else String.format("%.2f", ans)
}
