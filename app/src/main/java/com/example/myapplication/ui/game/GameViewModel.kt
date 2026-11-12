package com.grupo.sudoku.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.logic.Difficulty
import com.example.myapplication.logic.SudokuGenerator
import com.example.myapplication.logic.SudokuValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
enum class GameStatus { LOADING, PLAYING, WON }
data class GameUiState(
    val status: GameStatus = GameStatus.LOADING,
    val difficulty: Difficulty = Difficulty.MEDIO,
    val board: List<Int> = List(81) { 0 },
    val fixedCells: Set<Int> = emptySet(),
    val wrongCells: Set<Int> = emptySet(),
    val selectedIndex: Int? = null,
    val errors: Int = 0
) {
    val remainingNumbers: List<Int>
        get() = (1..9).map { number -> 9 - board.count { it == number } }
}
class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private var solution: List<Int> = emptyList()
    private var started = false
    fun start(difficulty: Difficulty) {
        if (started) return
        started = true
        newGame(difficulty)
    }
    fun newGame(difficulty: Difficulty) {
        _uiState.value = GameUiState(status = GameStatus.LOADING, difficulty
        = difficulty)
        viewModelScope.launch {
            val generated = withContext(Dispatchers.Default) {
                SudokuGenerator.generate(difficulty) }
            solution = generated.solution
            _uiState.value = GameUiState(
                status = GameStatus.PLAYING,
                difficulty = difficulty,
                board = generated.puzzle,
                fixedCells = generated.puzzle.indices.filter {
                    generated.puzzle[it] != 0 }.toSet()
            )
        }
    }
    fun selectCell(index: Int) {
        if (_uiState.value.status != GameStatus.PLAYING) return
        _uiState.update { it.copy(selectedIndex = index) }
    }
    fun enterNumber(number: Int) {
        val state = _uiState.value
        val index = state.selectedIndex ?: return
        if (state.status != GameStatus.PLAYING || index in state.fixedCells)
            return
        if (state.board[index] == number) return
        val newBoard = state.board.toMutableList().also { it[index] = number
        }
        val isWrong = number != solution[index]
        _uiState.update {
            it.copy(
                board = newBoard,
                errors = if (isWrong) it.errors + 1 else it.errors,
                wrongCells = if (isWrong) it.wrongCells + index else
                    it.wrongCells - index
            )
        }
        if (SudokuValidator.isSolved(newBoard, solution)) {
            _uiState.update { it.copy(status = GameStatus.WON, selectedIndex
            = null) }
        }
    }
    fun erase() {
        val state = _uiState.value
        val index = state.selectedIndex ?: return
        if (state.status != GameStatus.PLAYING || index in state.fixedCells)
            return
        _uiState.update {
            it.copy(
                board = it.board.toMutableList().also { board -> board[index] = 0 },
                wrongCells = it.wrongCells - index

            )
        }
    }
}