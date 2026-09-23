package br.com.meirelesefreitas.go.checkonline.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.meirelesefreitas.go.checkonline.data.local.UserPreferencesRepository
import br.com.meirelesefreitas.go.checkonline.data.model.Colaborador
import br.com.meirelesefreitas.go.checkonline.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LoginUiState(
    val matricula: String = "",
    val senha: String = "",
    val isPasswordVisible: Boolean = false,
    val savePassword: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginNavigationEvent {
    data class NavigateToMain(val colaborador: Colaborador) : LoginNavigationEvent()
    data class NavigateToFirstAccess(val matricula: String, val mode: Long) : LoginNavigationEvent()
    data class ShowMessage(val message: String) : LoginNavigationEvent()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LoginNavigationEvent>()
    val navigationEvent: SharedFlow<LoginNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            if (prefs.savedMatricula.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    matricula = prefs.savedMatricula,
                    senha = prefs.savedSenha,
                    savePassword = prefs.savePasswordEnabled
                )
            }
        }
    }

    fun onMatriculaChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            matricula = value.filter { it.isDigit() },
            errorMessage = null
        )
    }

    fun onSenhaChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            senha = value,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onSavePasswordChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(savePassword = checked)
    }

    fun login() {
        val state = _uiState.value
        if (state.matricula.isBlank() || state.senha.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Informe a matrícula e a senha")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = authRepository.getColaborador(state.matricula)
            result.onSuccess { colaborador ->
                if (colaborador == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login/Senha não confere"
                    )
                    return@onSuccess
                }

                // Check mode
                when (colaborador.mode) {
                    1L -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _navigationEvent.emit(
                            LoginNavigationEvent.ShowMessage("Conta necessita de Primeiro Acesso")
                        )
                        _navigationEvent.emit(
                            LoginNavigationEvent.NavigateToFirstAccess(colaborador.matricula, 1L)
                        )
                    }
                    2L -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _navigationEvent.emit(
                            LoginNavigationEvent.ShowMessage("Redefinição de senha pendente")
                        )
                        _navigationEvent.emit(
                            LoginNavigationEvent.NavigateToFirstAccess(colaborador.matricula, 2L)
                        )
                    }
                    else -> {
                        // Mode 0 - Active
                        if (colaborador.senha == state.senha) {
                            preferencesRepository.saveLoginCredentials(
                                matricula = state.matricula,
                                senha = state.senha,
                                savePassword = state.savePassword
                            )
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            _navigationEvent.emit(LoginNavigationEvent.NavigateToMain(colaborador))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Login/Senha não confere"
                            )
                        }
                    }
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao conectar ao servidor: ${e.localizedMessage}"
                )
            }
        }
    }

    fun handleFirstAccessClick() {
        val state = _uiState.value
        if (state.matricula.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Digite sua matrícula para o Primeiro Acesso")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepository.getColaborador(state.matricula)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { colaborador ->
                if (colaborador == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Matrícula não encontrada")
                } else {
                    val targetMode = if (colaborador.mode == 2L) 2L else 1L
                    _navigationEvent.emit(
                        LoginNavigationEvent.NavigateToFirstAccess(colaborador.matricula, targetMode)
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro ao buscar matrícula: ${e.localizedMessage}"
                )
            }
        }
    }
}
