package com.example.myaplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.RepositoryResult
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.logic.Difficulty
import com.example.myapplication.data.local.entity.PuzzleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = true,
    val puzzles: List<PuzzleEntity> = emptyList(),
    val filter: Difficulty? = null,
    val countByDifficulty: Map<String, Int> = emptyMap(),
    val countFromApi: Int = 0,
    val isImporting: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class AdminViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var allPuzzles: List<PuzzleEntity> = emptyList()

    init {
        viewModelScope.launch {
            repository.puzzles
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Erro ao ler puzzles: ${e.message}")
                    }
                }
                .collect { puzzles ->
                    allPuzzles = puzzles
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            countByDifficulty = puzzles
                                .groupBy { p -> Difficulty.fromName(p.difficulty).label }
                                .mapValues { (_, list) -> list.size },
                            countFromApi = puzzles.count { p -> p.source == "API" }
                        )
                    }
                    applyFilter()
                }
        }
    }

    private fun applyFilter() {
        val filter = _uiState.value.filter
        _uiState.update {
            it.copy(puzzles = allPuzzles.filter { p -> filter == null || p.difficulty == filter.name })
        }
    }

    fun onFilterChange(difficulty: Difficulty?) {
        _uiState.update { it.copy(filter = difficulty) }
        applyFilter()
    }

    fun importFromApi(quantity: Int = 3) {
        if (_uiState.value.isImporting) return
        _uiState.update { it.copy(isImporting = true, message = null, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.importPuzzlesFromApi(quantity)) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(isImporting = false, message = "${result.data} puzzle(s) importado(s) da API!")
                }
                is RepositoryResult.Error -> _uiState.update {
                    it.copy(isImporting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deletePuzzle(puzzle: PuzzleEntity) {
        viewModelScope.launch {
            when (val result = repository.deletePuzzle(puzzle)) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(message = "\"${puzzle.name}\" excluído.")
                }
                is RepositoryResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun dismissMessages() = _uiState.update { it.copy(message = null, errorMessage = null) }
}