package br.com.meirelesefreitas.go.checkonline.ui.screens.profile

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

data class ProfileUiState(
    val colaborador: Colaborador? = null,
    val isEditDialogVisible: Boolean = false,
    val editTelefone: String = "",
    val editPlaca: String = "",
    val editVenCnh: String = "",
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed class ProfileNavEvent {
    object LoggedOut : ProfileNavEvent()
    data class ShowMessage(val message: String) : ProfileNavEvent()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<ProfileNavEvent>()
    val navEvent: SharedFlow<ProfileNavEvent> = _navEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val matricula = prefs.activeMatricula.ifEmpty { prefs.savedMatricula }

            if (matricula.isNotEmpty()) {
                val colab = authRepository.getColaborador(matricula).getOrNull()
                _uiState.value = _uiState.value.copy(
                    colaborador = colab,
                    editTelefone = colab?.telefone ?: "",
                    editPlaca = colab?.placa ?: "",
                    editVenCnh = colab?.venCnh ?: "",
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun openEditDialog() {
        val colab = _uiState.value.colaborador
        _uiState.value = _uiState.value.copy(
            isEditDialogVisible = true,
            editTelefone = colab?.telefone ?: "",
            editPlaca = colab?.placa ?: "",
            editVenCnh = colab?.venCnh ?: ""
        )
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(isEditDialogVisible = false)
    }

    fun onEditTelefoneChanged(value: String) { _uiState.value = _uiState.value.copy(editTelefone = value) }
    fun onEditPlacaChanged(value: String) { _uiState.value = _uiState.value.copy(editPlaca = value.uppercase()) }
    fun onEditVenCnhChanged(value: String) { _uiState.value = _uiState.value.copy(editVenCnh = value) }

    fun saveProfile() {
        val state = _uiState.value
        val colab = state.colaborador ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            val result = authRepository.updateProfile(
                matricula = colab.matricula,
                telefone = state.editTelefone,
                placa = state.editPlaca,
                venCnh = state.editVenCnh
            )

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    colaborador = colab.copy(
                        telefone = state.editTelefone,
                        placa = state.editPlaca,
                        venCnh = state.editVenCnh
                    ),
                    isSaving = false,
                    isEditDialogVisible = false
                )
                _navEvent.emit(ProfileNavEvent.ShowMessage("Dados atualizados com sucesso!"))
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Erro ao atualizar: ${e.localizedMessage}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesRepository.clearSession()
            _navEvent.emit(ProfileNavEvent.LoggedOut)
        }
    }
}
