package com.ujjwal.speedmath.model

/**
 * All practice modes available in SpeedMath.
 * [displayName] is shown in the UI; [icon] is the math symbol on the mode card.
 */
enum class PracticeMode(val displayName: String, val icon: String) {
    ADDITION       ("Addition",       "+"),
    SUBTRACTION    ("Subtraction",    "−"),
    MULTIPLICATION ("Multiplication", "×"),
    DIVISION       ("Division",       "÷"),
    MIXED          ("Mixed",          "∑"),
    SQUARES        ("Squares",        "x²"),
    SQUARE_ROOTS   ("Square Roots",   "√"),
    CUBES          ("Cubes",          "x³"),
    CUBE_ROOTS     ("Cube Roots",     "∛")
}
