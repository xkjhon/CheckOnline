package br.com.meirelesefreitas.go.checkonline.data.repository

import android.content.Context
import br.com.meirelesefreitas.go.checkonline.data.local.ChecklistLocalDbHelper
import br.com.meirelesefreitas.go.checkonline.data.model.ChecklistDoc
import br.com.meirelesefreitas.go.checkonline.data.sync.SyncManager
import br.com.meirelesefreitas.go.checkonline.utils.DateUtils
import br.com.meirelesefreitas.go.checkonline.utils.NetworkMonitor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class ChecklistRepository(
    private val context: Context? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val localDb: ChecklistLocalDbHelper? = context?.let { ChecklistLocalDbHelper.getInstance(it) }
    private val syncManager: SyncManager? = context?.let { SyncManager.getInstance(it) }
    private val networkMonitor: NetworkMonitor? = context?.let { NetworkMonitor.getInstance(it) }

    private val colaboradoresCollection = firestore.collection("colaboradores")

    fun getDocsCollection(matricula: String) =
        colaboradoresCollection.document(matricula.trim()).collection("docs")

    suspend fun hasAnsweredToday(matricula: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanMatricula = matricula.trim()
        if (cleanMatricula.isEmpty()) return@withContext Result.success(false)

        // 1. Quando online: prioriza o Firebase e reconcilia o banco local (exclui registros apagados no servidor)
        if (networkMonitor?.isCurrentlyOnline() == true) {
            try {
                val snapshot = getDocsCollection(cleanMatricula).get().await()
                val serverDocs = snapshot.documents.map { ChecklistDoc.fromSnapshot(it) }
                localDb?.syncServerChecklists(cleanMatricula, serverDocs)

                val answered = localDb?.hasAnsweredToday(cleanMatricula) ?: serverDocs.any { DateUtils.isToday(it.data) }
                return@withContext Result.success(answered)
            } catch (e: Exception) {
                // Em caso de falha de conexão, consulta o banco local
                val localAnswered = localDb?.hasAnsweredToday(cleanMatricula) ?: false
                return@withContext Result.success(localAnswered)
            }
        }

        // 2. Quando offline: consulta o banco local SQLite
        val localAnswered = localDb?.hasAnsweredToday(cleanMatricula) ?: false
        return@withContext Result.success(localAnswered)
    }

    suspend fun submitChecklist(
        matricula: String,
        localidade: String,
        answers: Map<Int, Int>,
        observacao: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanMatricula = matricula.trim()
        val cleanLocalidade = localidade.trim()
        val now = Date()

        // 1. Se estiver online: grava diretamente no Firebase e espelha no banco local (evita qualquer duplicação)
        if (networkMonitor?.isCurrentlyOnline() == true) {
            try {
                val docsRef = getDocsCollection(cleanMatricula)
                val currentDocs = docsRef.get().await()

                var maxId = 0
                for (doc in currentDocs.documents) {
                    val numId = doc.id.toIntOrNull()
                    if (numId != null && numId > maxId) {
                        maxId = numId
                    }
                }
                val nextId = (maxId + 1).toString()

                val docData = mutableMapOf<String, Any>(
                    "data" to Timestamp(now),
                    "observacao" to observacao.trim(),
                    "matricula" to cleanMatricula,
                    "localidade" to cleanLocalidade,
                    "cidade" to cleanLocalidade
                )
                for ((key, value) in answers) {
                    docData[key.toString()] = value
                }

                docsRef.document(nextId).set(docData).await()

                // Salva no banco local com o ID oficial e já sincronizado
                val officialDoc = ChecklistDoc(
                    id = nextId,
                    data = now,
                    observacao = observacao.trim(),
                    matricula = cleanMatricula,
                    localidade = cleanLocalidade,
                    answers = answers,
                    isPendingSync = false
                )
                localDb?.saveChecklist(officialDoc, isPendingSync = false)

                return@withContext Result.success(nextId)
            } catch (e: Exception) {
                // Em caso de falha inesperada na rede, cai no salvamento offline abaixo
            }
        }

        // 2. Se estiver offline: salva no banco local com ID temporário e flag pendente de sincronização
        val tempLocalId = "temp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val localDoc = ChecklistDoc(
            id = tempLocalId,
            data = now,
            observacao = observacao.trim(),
            matricula = cleanMatricula,
            localidade = cleanLocalidade,
            answers = answers,
            isPendingSync = true
        )
        localDb?.saveChecklist(localDoc, isPendingSync = true)

        Result.success(tempLocalId)
    }

    fun getHistoryFlow(matricula: String): Flow<List<ChecklistDoc>> = callbackFlow {
        val cleanMatricula = matricula.trim()

        // 1. Emite o histórico local imediatamente
        val initialLocalList = localDb?.getChecklistsForMatricula(cleanMatricula) ?: emptyList()
        trySend(initialLocalList)

        // 2. Se online, escuta o Firestore em tempo real e reconcilia o banco local
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null
        if (networkMonitor?.isCurrentlyOnline() == true) {
            firestoreListener = getDocsCollection(cleanMatricula)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val serverList = snapshot.documents.map { ChecklistDoc.fromSnapshot(it) }
                        localDb?.syncServerChecklists(cleanMatricula, serverList)

                        val mergedList = localDb?.getChecklistsForMatricula(cleanMatricula) ?: serverList
                        trySend(mergedList)
                    }
                }
        }

        // Também atualiza o fluxo quando o SyncManager concluir uma sincronização
        val syncJob = scope.launch {
            syncManager?.syncEvents?.collect {
                val updatedLocal = localDb?.getChecklistsForMatricula(cleanMatricula) ?: emptyList()
                trySend(updatedLocal)
            }
        }

        awaitClose {
            firestoreListener?.remove()
            syncJob.cancel()
        }
    }

    suspend fun getAllChecklists(matricula: String): Result<List<ChecklistDoc>> = withContext(Dispatchers.IO) {
        val cleanMatricula = matricula.trim()

        // Se online, sincroniza com o servidor primeiro
        if (networkMonitor?.isCurrentlyOnline() == true && syncManager != null) {
            try {
                syncManager.syncAll(cleanMatricula)
            } catch (_: Exception) {}
        }

        val localList = localDb?.getChecklistsForMatricula(cleanMatricula) ?: emptyList()
        Result.success(localList)
    }

    fun getPendingCount(matricula: String): Int {
        return localDb?.getAllPendingChecklistsCount(matricula) ?: 0
    }
}
