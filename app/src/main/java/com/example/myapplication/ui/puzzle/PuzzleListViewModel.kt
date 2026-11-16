package com.example.myaplication.ui.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.RepositoryResult
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.logic.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PuzzleSort(val label: String) {
    RECENTES("Recentes"),
    NOME("Nome"),
    DIFICULDADE("Dificuldade")
}

data class PuzzleItemUi(
    val id: Long,
    val name: String,
    val difficulty: Difficulty,
    val emptyCells: Int,
    val source: String,
    val bestScore: Int?
)

data class PuzzleListUiState(
    val isLoading: Boolean = true,
    val playerName: String = "",
    val items: List<PuzzleItemUi> = emptyList(),
    val search: String = "",
    val filter: Difficulty? = null,
    val sort: PuzzleSort = PuzzleSort.RECENTES,
    val errorMessage: String? = null
)

class PuzzleListViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PuzzleListUiState())
    val uiState: StateFlow<PuzzleListUiState> = _uiState.asStateFlow()

    private data class Filters(val search: String, val difficulty: Difficulty?, val sort: PuzzleSort)
    private val filters = MutableStateFlow(Filters("", null, PuzzleSort.RECENTES))

    init {
        viewModelScope.launch {
            repository.session.collect { session ->
                _uiState.update { it.copy(playerName = session?.name.orEmpty()) }
            }
        }

        viewModelScope.launch {
            val result = repository.ensureInitialPuzzles()
            if (result is RepositoryResult.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
            try {
                val settings = repository.settings.first()
                val defaultDifficulty = Difficulty.entries.firstOrNull { it.name == settings.defaultDifficulty }
                onFilterChange(defaultDifficulty)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Não foi possível ler as configurações.") }
            }
        }

        viewModelScope.launch {
            combine(repository.puzzles, repository.scores, filters) { puzzles, scores, f ->
                val bestByPuzzle = scores
                    .groupBy { it.puzzleName }
                    .mapValues { (_, list) -> list.maxOf { it.score } }

                puzzles
                    .filter { f.difficulty == null || it.difficulty == f.difficulty.name }
                    .filter { it.name.contains(f.search.trim(), ignoreCase = true) }
                    .map { entity ->
                        PuzzleItemUi(
                            id = entity.id,
                            name = entity.name,
                            difficulty = Difficulty.fromName(entity.difficulty),
                            emptyCells = entity.givens.count { it == '0' },
                            source = entity.source,
                            bestScore = bestByPuzzle[entity.name]
                        )
                    }
                    .let { list ->
                        when (f.sort) {
                            PuzzleSort.RECENTES -> list
                            PuzzleSort.NOME -> list.sortedBy { it.name.lowercase() }
                            PuzzleSort.DIFICULDADE -> list.sortedBy { it.difficulty.ordinal }
                        }
                    }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao carregar: ${e.message}") }
                }
                .collect { items ->
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
        }
    }

    fun onSearchChange(text: String) {
        filters.update { it.copy(search = text) }
        _uiState.update { it.copy(search = text) }
    }
}