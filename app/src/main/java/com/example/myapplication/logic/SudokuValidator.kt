package com.example.myapplication.logic

object SudokuValidator {
    const val SIZE = 9
    const val CELLS = 81

    fun canPlace(board: List<Int>, index: Int, value: Int): Boolean {
        val row = index / SIZE
        val col = index % SIZE
        for (i in 0 until SIZE) {
            val rowIndex = row * SIZE + i
            val colIndex = i * SIZE + col
            if (rowIndex != index && board[rowIndex] == value) return false
            if (colIndex != index && board[colIndex] == value) return false
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                val boxIndex = r * SIZE + c
                if (boxIndex != index && board[boxIndex] == value) return false
            }
        }
        return true
    }
    fun findConflicts(board: List<Int>): Set<Int> =
        board.indices
            .filter { board[it] != 0 && !canPlace(board, it, board[it]) }
            .toSet()
    fun isSolved(board: List<Int>, solution: List<Int>): Boolean = board ==
            solution
    fun isValidSolution(solution: List<Int>): Boolean =
        solution.size == CELLS &&
                solution.all { it in 1..9 } &&
                findConflicts(solution).isEmpty()
    fun puzzleMatchesSolution(puzzle: List<Int>, solution: List<Int>):
            Boolean =
        puzzle.size == CELLS &&
                puzzle.indices.all { puzzle[it] == 0 || puzzle[it] ==
                        solution[it] }
}