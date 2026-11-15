package com.example.myaplication.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.RepositoryResult
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.logic.BoardFormat
import com.example.myaplication.logic.Difficulty
import com.example.myaplication.logic.SudokuValidator
import com.example.myapplication.data.local.entity.ScoreEntity
import com.example.myapplication.data.local.entity.SettingsEntity
import com.example.myapplication.logic.ScoreCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class GameStatus { LOADING, PLAYING, WON, LOST, ERROR }
data class GameUiState(
    val status: GameStatus = GameStatus.LOADING,
    val puzzleName: String = "",
val difficulty: Difficulty = Difficulty.MEDIO,
val board: List<Int> = List(81) { 0 },
val fixedCells: Set<Int> = emptySet(),
val wrongCells: Set<Int> = emptySet(),
val selectedIndex: Int? = null,
val errors: Int = 0,
val maxErrors: Int = 3,
val hintsUsed: Int = 0,
val seconds: Int = 0,
val showErrors: Boolean = true,
val finalScore: Int = 0,
val errorMessage: String? = null
) {
    val remainingNumbers: List<Int>
    get() = (1..9).map { number -> 9 - board.count { it == number } }
}
class GameViewModel(private val repository: SudokuRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private var solution: List<Int> = emptyList()
    private var initialBoard: List<Int> = emptyList()
    private var loadedPuzzleId: Long = -1
    private var timerJob: Job? = null
    fun loadPuzzle(puzzleId: Long) {
        if (puzzleId == loadedPuzzleId) return
        loadedPuzzleId = puzzleId
        viewModelScope.launch {
            val settings = try {
                repository.settings.first()
            } catch (e: Exception) {
                SettingsEntity()
            }
            when (val result = repository.getPuzzle(puzzleId)) {
                is RepositoryResult.Success -> {
                    val puzzle = result.data
                    solution = BoardFormat.fromText(puzzle.solution)
                    initialBoard = BoardFormat.fromText(puzzle.givens)
                    _uiState.value = GameUiState(
                        status = GameStatus.PLAYING,
                        puzzleName = puzzle.name,
                    difficulty = Difficulty.fromName(puzzle.difficulty),
                    board = initialBoard,
                    fixedCells = initialBoard.indices.filter {
                        initialBoard[it] != 0 }.toSet(),
                    maxErrors = settings.maxErrors,
                    showErrors = settings.showErrors
                    )
                    startTimer()
                }
                is RepositoryResult.Error -> _uiState.update {
                    it.copy(status = GameStatus.ERROR, errorMessage =
                        result.message)
                }
            }
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
        val newErrors = if (isWrong) state.errors + 1 else state.errors
        _uiState.update {
            it.copy(
                board = newBoard,
                errors = newErrors,
                wrongCells = if (isWrong) it.wrongCells + index else
                    it.wrongCells - index
            )
        }
        when {
            SudokuValidator.isSolved(newBoard, solution) -> finishGame(won =
                true)
                state.maxErrors > 0 && newErrors >= state.maxErrors ->
            finishGame(won = false)
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
    fun useHint() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val target = state.selectedIndex
            ?.takeIf { it !in state.fixedCells && state.board[it] !=
                    solution[it] }
            ?: state.board.indices.firstOrNull { state.board[it] !=
                    solution[it] }
            ?: return
        val newBoard = state.board.toMutableList().also { it[target] =
            solution[target] }
        _uiState.update {
            it.copy(
                board = newBoard,
                fixedCells = it.fixedCells + target,
                wrongCells = it.wrongCells - target,
                hintsUsed = it.hintsUsed + 1,
                selectedIndex = target
            )
        }
        if (SudokuValidator.isSolved(newBoard, solution)) finishGame(won =
            true)
    }
    fun restart() {
        if (initialBoard.isEmpty()) return
        _uiState.update {
            it.copy(
                status = GameStatus.PLAYING,
                board = initialBoard,
                fixedCells = initialBoard.indices.filter { i ->
                    initialBoard[i] != 0 }.toSet(),
                wrongCells = emptySet(),
                selectedIndex = null,
                errors = 0,
                hintsUsed = 0,
                seconds = 0,
                finalScore = 0,
                errorMessage = null
            )
        }
        startTimer()
    }
    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
    private fun finishGame(won: Boolean) {
        timerJob?.cancel()
        val state = _uiState.value
        if (!won) {
            _uiState.update { it.copy(status = GameStatus.LOST, selectedIndex
            = null) }
            return
        }
        val score = ScoreCalculator.calculate(state.difficulty,
            state.seconds, state.errors, state.hintsUsed)
        _uiState.update { it.copy(status = GameStatus.WON, finalScore =
            score, selectedIndex = null) }
        viewModelScope.launch {
            val result = repository.saveScore(
                ScoreEntity(
                    playerName = repository.session.value?.name ?: "Jogador",
                    puzzleName = state.puzzleName,
                    difficulty = state.difficulty.name,
                    score = score,
                    timeSeconds = state.seconds,
                    errors = state.errors
                )
            )
            if (result is RepositoryResult.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(seconds = it.seconds + 1) }
            }
        }
    }
}
