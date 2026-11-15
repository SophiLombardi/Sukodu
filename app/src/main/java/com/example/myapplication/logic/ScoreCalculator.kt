package com.example.myapplication.logic

import com.example.myaplication.logic.Difficulty
import kotlin.math.max

object ScoreCalculator {
    const val ERROR_PENALTY = 50
    const val HINT_PENALTY = 100
    fun calculate(difficulty: Difficulty, seconds: Int, errors: Int, hints:
    Int): Int {
        val total = difficulty.baseScore - seconds - errors * ERROR_PENALTY -
                hints * HINT_PENALTY
        return max(0, total)
    }
}