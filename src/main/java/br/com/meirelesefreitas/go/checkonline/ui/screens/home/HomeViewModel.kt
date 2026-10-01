package br.com.meirelesefreitas.go.checkonline.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.meirelesefreitas.go.checkonline.data.local.UserPreferencesRepository
import br.com.meirelesefreitas.go.checkonline.data.model.Colaborador
import br.com.meirelesefreitas.go.checkonline.data.repository.AuthRepository
import br.com.meirelesefreitas.go.checkonline.data.repository.ChecklistRepository
import br.com.meirelesefreitas.go.checkonline.data.sync.SyncEvent
import br.com.meirelesefreitas.go.checkonline.data.sync.SyncManager
import br.com.meirelesefreitas.go.checkonline.utils.DateUtils
import br.com.meirelesefreitas.go.checkonline.utils.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val colaborador: Colaborador? = null,
    val currentTimeString: String = "",
    val currentDateFullString: String = "",
    val hasAnsweredToday: Boolean = false,
    val pendingBusinessDays: List<LocalDate> = emptyList(),
    val daysWithoutSync: Long = 0,
    val pendingSyncCount: Int = 0,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val checklistRepository = ChecklistRepository(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    private val networkMonitor = NetworkMonitor.getInstance(application)
    private val syncManager = SyncManager.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        startRealtimeClock()
        observeNetwork()
        observeSyncEvents()
        loadData()
    }

    private fun startRealtimeClock() {
        viewModelScope.launch {
            while (isActive) {
                val now = Date()
                _uiState.value = _uiState.value.copy(
                    currentTimeString = DateUtils.formatCurrentTime(now),
                    currentDateFullString = DateUtils.formatCurrentDateFull(now)
                )
                delay(1000)
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnlineFlow.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (isOnline) {
                    loadData()
                }
            }
        }
    }

    private fun observeSyncEvents() {
        viewModelScope.launch {
            syncManager.syncEvents.collect { event ->
                when (event) {
                    is SyncEvent.SyncSuccess -> {
                        loadData()
                        if (event.syncedPendingCount > 0) {
                            _snackbarEvent.emit("${event.syncedPendingCount} checklist(s) sincronizado(s) com sucesso!")
                        }
                    }
                    is SyncEvent.SyncError -> {
                        _snackbarEvent.emit("Aviso: ${event.message}")
                    }
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val matricula = prefs.activeMatricula.ifEmpty { prefs.savedMatricula }

            if (matricula.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Nenhum usuário autenticado"
                )
                return@launch
            }

            // Load Colaborador details (from local SQLite or Firestore)
            val colabResult = authRepository.getColaborador(matricula)
            val colab = colabResult.getOrNull()

            // Check if already answered today (from local SQLite DB)
            val answeredTodayResult = checklistRepository.hasAnsweredToday(matricula)
            val hasAnsweredToday = answeredTodayResult.getOrDefault(false)

            val allChecklistsResult = checklistRepository.getAllChecklists(matricula)
            val allChecklists = allChecklistsResult.getOrDefault(emptyList())

            val answeredDates = allChecklists.mapNotNull { it.data }
            val pendingDays = DateUtils.findPendingBusinessDays(answeredDates)

            val pendingSyncCount = checklistRepository.getPendingCount(matricula)

            // Calculate days without sync
            val lastSyncTime = prefs.lastSyncTimeMillis
            val diffMillis = System.currentTimeMillis() - lastSyncTime
            val daysDiff = TimeUnit.MILLISECONDS.toDays(diffMillis)

            _uiState.value = _uiState.value.copy(
                colaborador = colab,
                hasAnsweredToday = hasAnsweredToday,
                pendingBusinessDays = pendingDays,
                pendingSyncCount = pendingSyncCount,
                daysWithoutSync = daysDiff,
                isOnline = networkMonitor.isCurrentlyOnline(),
                isLoading = false
            )
        }
    }

    fun synchronize() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val matricula = prefs.activeMatricula.ifEmpty { prefs.savedMatricula }

            if (matricula.isNotEmpty()) {
                val syncResult = syncManager.syncAll(matricula)
                syncResult.onSuccess { (syncedCount, downloaded) ->
                    loadData()
                    _uiState.value = _uiState.value.copy(isSyncing = false)
                    _snackbarEvent.emit("Sincronização concluída! ($downloaded vistorias no dispositivo)")
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isSyncing = false)
                    _snackbarEvent.emit(e.localizedMessage ?: "Erro na sincronização")
                }
            } else {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }
}
