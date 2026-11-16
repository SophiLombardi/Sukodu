package com.example.myaplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.RepositoryResult
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.logic.BoardFormat
import com.example.myaplication.logic.Difficulty
import com.example.myapplication.data.local.entity.PuzzleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PuzzleEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val name: String = "",
    val difficulty: Difficulty = Difficulty.MEDIO,
    val preview: List<Int> = emptyList(),
    val source: String = "",
    val isSaving: Boolean = false,
    val finished: Boolean = false,
    val errorMessage: String? = null
)

class PuzzleEditViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PuzzleEditUiState())
    val uiState: StateFlow<PuzzleEditUiState> = _uiState.asStateFlow()

    private var original: PuzzleEntity? = null
    private var loadedId: Long = -1

    fun load(puzzleId: Long) {
        if (puzzleId == loadedId) return
        loadedId = puzzleId

        if (puzzleId == 0L) {
            _uiState.value = PuzzleEditUiState(isNew = true)
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getPuzzle(puzzleId)) {
                is RepositoryResult.Success -> {
                    val puzzle = result.data
                    original = puzzle
                    _uiState.value = PuzzleEditUiState(
                        isNew = false,
                        name = puzzle.name,
                        difficulty = Difficulty.fromName(puzzle.difficulty),
                        preview = BoardFormat.fromText(puzzle.givens),
                        source = puzzle.source
                    )
                }
                is RepositoryResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }

    fun onDifficultyChange(value: Difficulty) = _uiState.update { it.copy(difficulty = value) }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()

        if (name.length !in 3..30) {
            _uiState.update { it.copy(errorMessage = "O nome deve ter entre 3 e 30 caracteres.") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (state.isNew) {
                repository.createLocalPuzzle(name, state.difficulty)
            } else {
                val puzzle = original ?: return@launch
                repository.updatePuzzle(puzzle.copy(name = name, difficulty = state.difficulty.name))
            }

            when (result) {
                is RepositoryResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, finished = true) }
                }
                is RepositoryResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun delete() {
        val puzzle = original ?: return
        viewModelScope.launch {
            when (val result = repository.deletePuzzle(puzzle)) {
                is RepositoryResult.Success -> {
                    _uiState.update { it.copy(finished = true) }
                }
                is RepositoryResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }
}