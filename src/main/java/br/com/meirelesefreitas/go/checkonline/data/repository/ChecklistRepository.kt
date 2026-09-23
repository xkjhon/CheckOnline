package br.com.meirelesefreitas.go.checkonline.data.repository

import br.com.meirelesefreitas.go.checkonline.data.model.ChecklistDoc
import br.com.meirelesefreitas.go.checkonline.utils.DateUtils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChecklistRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colaboradoresCollection = firestore.collection("colaboradores")

    fun getDocsCollection(matricula: String) =
        colaboradoresCollection.document(matricula.trim()).collection("docs")

    suspend fun hasAnsweredToday(matricula: String): Result<Boolean> {
        return try {
            val snapshot = getDocsCollection(matricula)
                .get()
                .await()

            val answeredToday = snapshot.documents.any { doc ->
                val date = doc.getTimestamp("data")?.toDate()
                DateUtils.isToday(date)
            }
            Result.success(answeredToday)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitChecklist(
        matricula: String,
        localidade: String,
        answers: Map<Int, Boolean>,
        observacao: String
    ): Result<String> {
        return try {
            val docsRef = getDocsCollection(matricula)
            val currentDocs = docsRef.get().await()

            // Calculate next sequential integer ID ("1", "2", "3", ...)
            var maxId = 0
            for (doc in currentDocs.documents) {
                val numId = doc.id.toIntOrNull()
                if (numId != null && numId > maxId) {
                    maxId = numId
                }
            }
            val nextId = (maxId + 1).toString()

            val docData = mutableMapOf<String, Any>(
                "data" to FieldValue.serverTimestamp(),
                "observacao" to observacao.trim(),
                "matricula" to matricula.trim(),
                "localidade" to localidade.trim(),
                "cidade" to localidade.trim()
            )

            // Save question answers as boolean fields ("1", "2", ...)
            for ((key, value) in answers) {
                docData[key.toString()] = value
            }

            docsRef.document(nextId).set(docData).await()
            Result.success(nextId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getHistoryFlow(matricula: String): Flow<List<ChecklistDoc>> = callbackFlow {
        val listener = getDocsCollection(matricula)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.map { ChecklistDoc.fromSnapshot(it) }
                        .sortedWith(
                            compareByDescending<ChecklistDoc> { it.data?.time ?: 0L }
                                .thenByDescending { it.id.toIntOrNull() ?: 0 }
                        )
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllChecklists(matricula: String): Result<List<ChecklistDoc>> {
        return try {
            val snapshot = getDocsCollection(matricula).get().await()
            val list = snapshot.documents.map { ChecklistDoc.fromSnapshot(it) }
                .sortedWith(
                    compareByDescending<ChecklistDoc> { it.data?.time ?: 0L }
                        .thenByDescending { it.id.toIntOrNull() ?: 0 }
                )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
