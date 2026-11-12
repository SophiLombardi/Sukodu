package com.example.myapplication.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuLogicTest {
    @Test
    fun geradorCriaSolucaoValida() {
        val generated = SudokuGenerator.generate(Difficulty.MEDIO,
            Random(42))
        assertTrue(SudokuValidator.isValidSolution(generated.solution))
        assertTrue(SudokuValidator.puzzleMatchesSolution(generated.puzzle,
            generated.solution))
    }
    @Test
    fun puzzleGeradoTemSolucaoUnica() {
        val generated = SudokuGenerator.generate(Difficulty.DIFICIL,
            Random(7))
        assertEquals(1,
            SudokuGenerator.countSolutions(generated.puzzle.toMutableList()))
    }
    @Test
    fun numeroRepetidoNaLinhaNaoPode() {
        val board = MutableList(81) { 0 }
        board[0] = 5
        assertFalse(SudokuValidator.canPlace(board, 8, 5))
        assertFalse(SudokuValidator.canPlace(board, 72, 5))
        assertFalse(SudokuValidator.canPlace(board, 20, 5))
        assertTrue(SudokuValidator.canPlace(board, 40, 5))

    }
    @Test
    fun conversaoDeTextoIdaEVolta() {
        val board = List(81) { it % 10 }
        assertEquals(board, BoardFormat.fromText(BoardFormat.toText(board)))
        assertEquals("02:05", BoardFormat.formatTime(125))
    }
}