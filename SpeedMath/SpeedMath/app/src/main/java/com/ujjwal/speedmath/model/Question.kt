package com.ujjwal.speedmath.model

/**
 * One generated math question.
 *
 * @param text          Display string shown to user, e.g. "23 + 45"
 * @param answer        Exact numeric answer (used for validation)
 * @param displayAnswer Formatted string of the answer shown on wrong attempt
 * @param tolerance     Allowed error margin for non-perfect roots (default 0.0 = exact match)
 */
data class Question(
    val text: String,
    val answer: Double,
    val displayAnswer: String,
    val tolerance: Double = 0.0
)
