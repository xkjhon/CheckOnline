package br.com.meirelesefreitas.go.checkonline.ui.screens.checklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.meirelesefreitas.go.checkonline.data.local.UserPreferencesRepository
import br.com.meirelesefreitas.go.checkonline.data.model.ChecklistAnswer
import br.com.meirelesefreitas.go.checkonline.data.model.Colaborador
import br.com.meirelesefreitas.go.checkonline.data.repository.AuthRepository
import br.com.meirelesefreitas.go.checkonline.data.repository.ChecklistRepository
import br.com.meirelesefreitas.go.checkonline.utils.CheckListItems
import br.com.meirelesefreitas.go.checkonline.utils.ChecklistQuestion
import br.com.meirelesefreitas.go.checkonline.utils.ChecklistSection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChecklistUiState(
    val colaborador: Colaborador? = null,
    val sections: List<ChecklistSection> = CheckListItems.agcomSections,
    val questions: List<ChecklistQuestion> = CheckListItems.agcomQuestions,
    val answers: Map<Int, Int> = CheckListItems.agcomQuestions.associate { it.id to ChecklistAnswer.SIM.code }, // Default Código 1 (Sim)
    val observacao: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

sealed class ChecklistNavEvent {
    data class SubmittedSuccess(val newId: String) : ChecklistNavEvent()
    data class ShowError(val message: String) : ChecklistNavEvent()
}

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val checklistRepository = ChecklistRepository(application)
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<ChecklistNavEvent>()
    val navEvent: SharedFlow<ChecklistNavEvent> = _navEvent.asSharedFlow()

    init {
        loadColaborador()
    }

    private fun loadColaborador() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val matricula = prefs.activeMatricula.ifEmpty { prefs.savedMatricula }
            if (matricula.isNotEmpty()) {
                val colab = authRepository.getColaborador(matricula).getOrNull()
                val questions = CheckListItems.getQuestionsForAct(colab?.act)
                val sections = CheckListItems.getSectionsForAct(colab?.act)
                val initialAnswers = questions.associate { it.id to ChecklistAnswer.SIM.code }
                _uiState.value = _uiState.value.copy(
                    colaborador = colab,
                    sections = sections,
                    questions = questions,
                    answers = initialAnswers
                )
            }
        }
    }

    fun setAnswer(questionId: Int, code: Int) {
        val updated = _uiState.value.answers.toMutableMap()
        updated[questionId] = code
        _uiState.value = _uiState.value.copy(answers = updated)
    }

    fun setAllAnswers(code: Int = ChecklistAnswer.SIM.code) {
        val updated = _uiState.value.questions.associate { it.id to code }
        _uiState.value = _uiState.value.copy(answers = updated)
    }

    fun onObservacaoChanged(value: String) {
        _uiState.value = _uiState.value.copy(observacao = value)
    }

    fun submitChecklist() {
        val state = _uiState.value
        val colab = state.colaborador
        if (colab == null) {
            _uiState.value = state.copy(errorMessage = "Dados do colaborador não encontrados.")
            return
        }

        if (state.questions.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Não há perguntas cadastradas para a sua função.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, errorMessage = null)

            val result = checklistRepository.submitChecklist(
                matricula = colab.matricula,
                localidade = colab.localidade,
                answers = state.answers,
                observacao = state.observacao
            )

            result.onSuccess { docId ->
                _uiState.value = _uiState.value.copy(isSubmitting = false)
                _navEvent.emit(ChecklistNavEvent.SubmittedSuccess(docId))
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Erro ao gravar checklist: ${e.localizedMessage}"
                )
                _navEvent.emit(ChecklistNavEvent.ShowError("Erro ao gravar: ${e.localizedMessage}"))
            }
        }
    }
}
