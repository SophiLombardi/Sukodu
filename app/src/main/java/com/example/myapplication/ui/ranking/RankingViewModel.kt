package com.example.myaplication.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.RepositoryResult
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.logic.BoardFormat
import com.example.myaplication.logic.Difficulty
import com.example.myapplication.data.local.entity.ScoreEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RankingItemUi(
    val position: Int,
    val score: ScoreEntity,
    val difficultyLabel: String,
    val timeText: String,
    val dateText: String
)

data class RankingUiState(
    val isLoading: Boolean = true,
    val items: List<RankingItemUi> = emptyList(),
    val filter: Difficulty? = null,
    val onlyMine: Boolean = false,
    val isAdmin: Boolean = false,
    val playerName: String = "",
    val editingScore: ScoreEntity? = null,
    val editName: String = "",
    val message: String? = null,
    val errorMessage: String? = null
)

class RankingViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private var allScores: List<ScoreEntity> = emptyList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))

    init {
        viewModelScope.launch {
            repository.session.collect { session ->
                _uiState.update { it.copy(isAdmin = session?.isAdmin == true, playerName = session?.name.orEmpty()) }
                applyFilters()
            }
        }
        viewModelScope.launch {
            repository.scores
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao ler ranking: ${e.message}") } }
                .collect { scores ->
                    allScores = scores
                    applyFilters()
                }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val items = allScores
            .filter { state.filter == null || it.difficulty == state.filter.name }
            .filter { !state.onlyMine || it.playerName.equals(state.playerName, ignoreCase = true) }
            .sortedByDescending { it.score }
            .mapIndexed { index, score ->
                RankingItemUi(
                    position = index + 1,
                    score = score,
                    difficultyLabel = Difficulty.fromName(score.difficulty).label,
                    timeText = BoardFormat.formatTime(score.timeSeconds),
                    dateText = dateFormat.format(Date(score.date))
                )
            }
        _uiState.update { it.copy(isLoading = false, items = items) }
    }

    fun onFilterChange(difficulty: Difficulty?) {
        _uiState.update { it.copy(filter = difficulty) }
        applyFilters()
    }

    fun onOnlyMineChange(value: Boolean) {
        _uiState.update { it.copy(onlyMine = value) }
        applyFilters()
    }

    fun startEdit(score: ScoreEntity) {
        if (!_uiState.value.isAdmin) return
        _uiState.update { it.copy(editingScore = score, editName = score.playerName) }
    }

    fun onEditNameChange(value: String) = _uiState.update { it.copy(editName = value) }

    fun cancelEdit() = _uiState.update { it.copy(editingScore = null) }

    fun saveEdit() {
        val state = _uiState.value
        val score = state.editingScore ?: return
        val newName = state.editName.trim()
        if (newName.length < 3) {
            _uiState.update { it.copy(errorMessage = "O nome precisa ter pelo menos 3 letras.") }
            return
        }
        viewModelScope.launch {
            when (val result = repository.updateScore(score.copy(playerName = newName))) {
                is RepositoryResult.Success -> _uiState.update { it.copy(editingScore = null, message = "Pontuação atualizada.") }
                is RepositoryResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun delete(score: ScoreEntity) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            when (val result = repository.deleteScore(score)) {
                is RepositoryResult.Success -> _uiState.update { it.copy(message = "Pontuação excluída.") }
                is RepositoryResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun clearAll() {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            when (val result = repository.clearScores()) {
                is RepositoryResult.Success -> _uiState.update { it.copy(message = "Ranking zerado.") }
                is RepositoryResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun dismissMessages() = _uiState.update { it.copy(message = null, errorMessage = null) }
}