package com.example.myaplication.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.RepositoryResult
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.logic.Difficulty
import com.example.myapplication.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val maxErrors: Int = 3,
    val showErrors: Boolean = true,
    val defaultDifficulty: Difficulty? = null,
    val userName: String = "",
    val isAdmin: Boolean = false,
    val errorMessage: String? = null
)
class SettingsViewModel(private val repository: SudokuRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            repository.collect { session -> _uiState.update { it.copy(userName = session?.name.orEmpty(),
                    isAdmin = session?.isAdmin == true) }
            }
        }
        viewModelScope.launch {
            repository.settings.catch { e -> _uiState.update { it.copy(errorMessage = "Erro ao ler configurações: ${e.message}") } }
                    .collect { settings -> _uiState.update {
                            it.copy(
                                maxErrors = settings.maxErrors,
                                showErrors = settings.showErrors,
                                defaultDifficulty =
                                    Difficulty.entries.firstOrNull { d -> d.name == settings.defaultDifficulty }
                            )
                        }
                    }
                }
                }
            fun onMaxErrorsChange(value: Int) = save(_uiState.value.copy(maxErrors =
                value))
            fun onShowErrorsChange(value: Boolean) =
                save(_uiState.value.copy(showErrors = value))
            fun onDefaultDifficultyChange(value: Difficulty?) =
                save(_uiState.value.copy(defaultDifficulty = value))
            private fun save(newState: SettingsUiState) {
                viewModelScope.launch {
                    val entity = SettingsEntity(
                        maxErrors = newState.maxErrors,
                        showErrors = newState.showErrors,
                        defaultDifficulty = newState.defaultDifficulty?.name ?: "TODAS"
                    )
                    val result = repository.saveSettings(entity)
                    if (result is RepositoryResult.Error) {
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }
                }
            }
            fun logout() = repository.logout()
            fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
        }
