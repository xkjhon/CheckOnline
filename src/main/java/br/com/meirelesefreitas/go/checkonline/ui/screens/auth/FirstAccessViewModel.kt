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
import kotlinx.coroutines.launch

data class FirstAccessUiState(
    val matricula: String = "",
    val mode: Long = 1L, // 1 = Primeiro Acesso, 2 = Redefinir Senha
    val nome: String = "",
    val localidade: String = "",
    val novaSenha: String = "",
    val confirmSenha: String = "",
    val telefone: String = "",
    val placa: String = "",
    val venCnh: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class FirstAccessNavEvent {
    object NavigateToHome : FirstAccessNavEvent()
    data class ShowMessage(val message: String) : FirstAccessNavEvent()
}

class FirstAccessViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(FirstAccessUiState())
    val uiState: StateFlow<FirstAccessUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<FirstAccessNavEvent>()
    val navigationEvent: SharedFlow<FirstAccessNavEvent> = _navigationEvent.asSharedFlow()

    fun initialize(matricula: String, mode: Long) {
        _uiState.value = _uiState.value.copy(matricula = matricula, mode = mode)
        loadColaboradorData(matricula)
    }

    private fun loadColaboradorData(matricula: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = authRepository.getColaborador(matricula)
            result.onSuccess { colab ->
                if (colab != null) {
                    _uiState.value = _uiState.value.copy(
                        nome = colab.nome,
                        localidade = colab.localidade,
                        telefone = colab.telefone,
                        placa = colab.placa,
                        venCnh = colab.venCnh,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Colaborador não encontrado"
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar dados: ${it.localizedMessage}"
                )
            }
        }
    }

    fun onNovaSenhaChanged(value: String) {
        _uiState.value = _uiState.value.copy(novaSenha = value, errorMessage = null)
    }

    fun onConfirmSenhaChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmSenha = value, errorMessage = null)
    }

    fun onTelefoneChanged(value: String) { _uiState.value = _uiState.value.copy(telefone = value) }
    fun onPlacaChanged(value: String) { _uiState.value = _uiState.value.copy(placa = value.uppercase()) }
    fun onVenCnhChanged(value: String) { _uiState.value = _uiState.value.copy(venCnh = value) }
    fun togglePasswordVisibility() { _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible) }

    fun submit() {
        val state = _uiState.value
        val senha = state.novaSenha.trim()
        val confirm = state.confirmSenha.trim()

        if (senha.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Informe a nova senha")
            return
        }

        // Validação: no mínimo 6 caracteres, contendo letras e números
        if (senha.length < 6) {
            _uiState.value = state.copy(errorMessage = "A senha deve ter no mínimo 6 caracteres")
            return
        }

        val hasLetters = senha.any { it.isLetter() }
        val hasDigits = senha.any { it.isDigit() }
        val isAlphanumericOnly = senha.all { it.isLetterOrDigit() }

        if (!isAlphanumericOnly || !hasLetters || !hasDigits) {
            _uiState.value = state.copy(errorMessage = "A senha deve conter letras e números (apenas letras e números)")
            return
        }

        if (senha != confirm) {
            _uiState.value = state.copy(errorMessage = "As senhas não coincidem")
            return
        }

        if (state.mode == 1L) {
            if (state.telefone.isBlank() || state.placa.isBlank() || state.venCnh.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Preencha todos os campos obrigatórios")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = if (state.mode == 1L) {
                authRepository.completeFirstAccess(
                    matricula = state.matricula,
                    novaSenha = senha,
                    telefone = state.telefone,
                    placa = state.placa,
                    venCnh = state.venCnh
                )
            } else {
                authRepository.resetPassword(
                    matricula = state.matricula,
                    novaSenha = senha
                )
            }

            result.onSuccess {
                preferencesRepository.saveLoginCredentials(
                    matricula = state.matricula,
                    senha = senha,
                    savePassword = true
                )
                _uiState.value = _uiState.value.copy(isLoading = false)
                _navigationEvent.emit(FirstAccessNavEvent.ShowMessage("Acesso configurado com sucesso!"))
                _navigationEvent.emit(FirstAccessNavEvent.NavigateToHome)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao salvar: ${e.localizedMessage}"
                )
            }
        }
    }
}
