package com.example.myaplication.logic

import kotlin.random.Random
data class GeneratedPuzzle(val puzzle: List<Int>, val solution: List<Int>)
object SudokuGenerator {
    fun generate(difficulty: Difficulty, random: Random = Random.Default):
            GeneratedPuzzle {
        val solution = MutableList(SudokuValidator.CELLS) { 0 }
        fillBoard(solution, random)
        val puzzle = solution.toMutableList()
        var removed = 0
        val positions = (0 until SudokuValidator.CELLS).shuffled(random)
        for (position in positions) {
            if (removed >= difficulty.cellsToRemove) break
            val backup = puzzle[position]
            puzzle[position] = 0
            if (countSolutions(puzzle.toMutableList(), limit = 2) == 1) {
                removed++
            } else {
                puzzle[position] = backup
            }
        }
        return GeneratedPuzzle(puzzle.toList(), solution.toList())
    }
    private fun fillBoard(board: MutableList<Int>, random: Random): Boolean {
        val index = board.indexOfFirst { it == 0 }
        if (index == -1) return true
        for (number in (1..9).shuffled(random)) {
            if (SudokuValidator.canPlace(board, index, number)) {
                board[index] = number
                if (fillBoard(board, random)) return true
                board[index] = 0
            }
        }
        return false
    }
    fun countSolutions(board: MutableList<Int>, limit: Int = 2): Int {
        val index = board.indexOfFirst { it == 0 }

        if (index == -1) return 1
        var count = 0
        for (number in 1..9) {
            if (SudokuValidator.canPlace(board, index, number)) {
                board[index] = number
                count += countSolutions(board, limit - count)
                board[index] = 0
                if (count >= limit) break
            }
        }
        return count
    }
}
