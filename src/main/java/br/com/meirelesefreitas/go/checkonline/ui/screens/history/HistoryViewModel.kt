package br.com.meirelesefreitas.go.checkonline.ui.screens.history

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.meirelesefreitas.go.checkonline.data.local.UserPreferencesRepository
import br.com.meirelesefreitas.go.checkonline.data.model.ChecklistDoc
import br.com.meirelesefreitas.go.checkonline.data.model.Colaborador
import br.com.meirelesefreitas.go.checkonline.data.repository.AuthRepository
import br.com.meirelesefreitas.go.checkonline.data.repository.ChecklistRepository
import br.com.meirelesefreitas.go.checkonline.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class HistoryFilter {
    ALL,
    LAST_7_DAYS,
    THIS_MONTH
}

data class HistoryUiState(
    val colaborador: Colaborador? = null,
    val allHistory: List<ChecklistDoc> = emptyList(),
    val filteredHistory: List<ChecklistDoc> = emptyList(),
    val currentFilter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = true,
    val isExportingPdf: Boolean = false,
    val errorMessage: String? = null
)

sealed class HistoryNavEvent {
    data class PdfGenerated(val uri: Uri) : HistoryNavEvent()
    data class ShowMessage(val message: String) : HistoryNavEvent()
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val checklistRepository = ChecklistRepository()
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<HistoryNavEvent>()
    val navEvent: SharedFlow<HistoryNavEvent> = _navEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val matricula = prefs.activeMatricula.ifEmpty { prefs.savedMatricula }

            if (matricula.isNotEmpty()) {
                val colab = authRepository.getColaborador(matricula).getOrNull()
                _uiState.value = _uiState.value.copy(colaborador = colab)

                // Observe history in real-time
                checklistRepository.getHistoryFlow(matricula).collect { list ->
                    _uiState.value = _uiState.value.copy(
                        allHistory = list,
                        filteredHistory = applyFilter(list, _uiState.value.currentFilter),
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Nenhum usuário ativo encontrado."
                )
            }
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _uiState.value = _uiState.value.copy(
            currentFilter = filter,
            filteredHistory = applyFilter(_uiState.value.allHistory, filter)
        )
    }

    private fun applyFilter(list: List<ChecklistDoc>, filter: HistoryFilter): List<ChecklistDoc> {
        val now = Calendar.getInstance()
        return when (filter) {
            HistoryFilter.ALL -> list
            HistoryFilter.LAST_7_DAYS -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val cutoff = cal.time
                list.filter { it.data != null && it.data.after(cutoff) }
            }
            HistoryFilter.THIS_MONTH -> {
                val currentMonth = now.get(Calendar.MONTH)
                val currentYear = now.get(Calendar.YEAR)
                list.filter { item ->
                    if (item.data == null) false
                    else {
                        val c = Calendar.getInstance().apply { time = item.data }
                        c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
                    }
                }
            }
        }
    }

    fun exportWeeklyPdf() {
        val state = _uiState.value
        val colab = state.colaborador
        if (colab == null) {
            viewModelScope.launch {
                _navEvent.emit(HistoryNavEvent.ShowMessage("Dados do colaborador não disponíveis para exportação."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExportingPdf = true)

            val weekHistory = applyFilter(state.allHistory, HistoryFilter.LAST_7_DAYS)
            val uri = PdfGenerator.generateWeeklySummaryPdf(
                context = getApplication(),
                colaborador = colab,
                checklists = if (weekHistory.isNotEmpty()) weekHistory else state.allHistory.take(7),
                periodDescription = "Últimos 7 Dias"
            )

            _uiState.value = _uiState.value.copy(isExportingPdf = false)

            if (uri != null) {
                _navEvent.emit(HistoryNavEvent.PdfGenerated(uri))
            } else {
                _navEvent.emit(HistoryNavEvent.ShowMessage("Erro ao gerar arquivo PDF."))
            }
        }
    }
}
