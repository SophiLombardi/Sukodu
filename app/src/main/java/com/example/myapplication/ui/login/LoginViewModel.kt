package com.example.myaplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.data.repository.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isAdminMode: Boolean = false,
    val name: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val session: UserSession? = null
)

class LoginViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.session.collect { session ->
                _uiState.update { it.copy(session = session) }
            }
        }
    }

    fun onModeChange(isAdmin: Boolean) =
        _uiState.update { it.copy(isAdminMode = isAdmin, errorMessage = null, password = "") }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun login() {
        val state = _uiState.value
        val name = state.name.trim()

        if (state.isAdminMode) {
            if (name == ADMIN_USER && state.password == ADMIN_PASSWORD) {
                repository.login(UserSession(name = "Administrador", isAdmin = true))
                _uiState.update { it.copy(password = "") }
            } else {
                _uiState.update { it.copy(errorMessage = "Usuário ou senha de administrador inválidos.") }
            }
        } else {
            when {
                name.length < 3 -> _uiState.update { it.copy(errorMessage = "O nome precisa ter pelo menos 3 letras.") }
                name.length > 15 -> _uiState.update { it.copy(errorMessage = "O nome pode ter no máximo 15 letras.") }
                else -> repository.login(UserSession(name = name, isAdmin = false))
            }
        }
    }

    fun logout() = repository.logout()
}