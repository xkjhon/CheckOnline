package br.com.meirelesefreitas.go.checkonline.data.repository

import br.com.meirelesefreitas.go.checkonline.data.model.Colaborador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colaboradoresCollection = firestore.collection("colaboradores")

    suspend fun getColaborador(matricula: String): Result<Colaborador?> {
        return try {
            val snapshot = colaboradoresCollection.document(matricula.trim()).get().await()
            var colaborador = Colaborador.fromSnapshot(snapshot)
            if (colaborador != null) {
                val superKey = colaborador.superRegiao.ifEmpty {
                    if (colaborador.supervisor.contains("_")) colaborador.supervisor else ""
                }

                val resolvedSupervisor = resolveSupervisorName(
                    superKey = superKey,
                    currentSupervisor = colaborador.supervisor,
                    localidade = colaborador.localidade
                )

                if (resolvedSupervisor.isNotBlank()) {
                    colaborador = colaborador.copy(supervisor = resolvedSupervisor)
                }
            }
            Result.success(colaborador)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveSupervisorName(
        superKey: String,
        currentSupervisor: String,
        localidade: String
    ): String {
        // Se o supervisor atual já for um nome próprio completo (não uma chave com underline)
        if (currentSupervisor.isNotBlank() && !currentSupervisor.contains("_") && currentSupervisor.trim().contains(" ")) {
            return currentSupervisor
        }

        val cleanCity = localidade.lowercase().trim()
        val mappedSuper = when {
            cleanCity.contains("rio verde") -> "sul_goiano"
            cleanCity.contains("jatai") || cleanCity.contains("jataí") -> "sul_goiano"
            cleanCity.contains("itumbiara") -> "sul_goiano"
            cleanCity.contains("caldas") -> "sul_goiano"
            cleanCity.contains("mineiros") -> "sul_goiano"
            cleanCity.contains("morrinhos") -> "sul_goiano"
            cleanCity.contains("goiania") || cleanCity.contains("goiânia") -> "metropolitana"
            cleanCity.contains("anapolis") || cleanCity.contains("anápolis") -> "centro_norte"
            else -> null
        }

        val searchKeys = listOfNotNull(
            superKey.ifEmpty { null },
            mappedSuper,
            superKey.lowercase().ifEmpty { null },
            superKey.replace("-", "_").ifEmpty { null },
            currentSupervisor.ifEmpty { null },
            localidade.lowercase().replace(" ", "_").ifEmpty { null },
            localidade.ifEmpty { null }
        ).distinct()

        // 1. Prioridade: Busca na coleção "adm" onde o campo super == superKey (ex: super == "sul_goiano")
        for (key in searchKeys) {
            try {
                val querySnap = firestore.collection("adm")
                    .whereEqualTo("super", key)
                    .get()
                    .await()
                if (!querySnap.isEmpty) {
                    val doc = querySnap.documents.first()
                    val name = doc.getString("nome")
                    if (!name.isNullOrBlank()) return name
                }
            } catch (_: Exception) {}
        }

        // 2. Busca na coleção "adm" diretamente pelo ID do documento (matrícula do supervisor)
        for (key in searchKeys) {
            try {
                val doc = firestore.collection("adm").document(key).get().await()
                if (doc.exists()) {
                    val name = doc.getString("nome")
                    if (!name.isNullOrBlank()) return name
                }
            } catch (_: Exception) {}
        }

        // 3. Busca na coleção "supervisores" por compatibilidade
        for (key in searchKeys) {
            try {
                val doc = firestore.collection("supervisores").document(key).get().await()
                if (doc.exists()) {
                    val name = doc.getString("nome") ?: doc.getString("supervisor")
                    if (!name.isNullOrBlank()) return name
                }
            } catch (_: Exception) {}
        }

        // 4. Busca na coleção "supervisores" por whereEqualTo super == key
        for (key in searchKeys) {
            try {
                val querySnap = firestore.collection("supervisores")
                    .whereEqualTo("super", key)
                    .get()
                    .await()
                if (!querySnap.isEmpty) {
                    val doc = querySnap.documents.first()
                    val name = doc.getString("nome") ?: doc.getString("supervisor")
                    if (!name.isNullOrBlank()) return name
                }
            } catch (_: Exception) {}
        }

        // Fallback de formatação caso nenhum documento seja encontrado
        if (currentSupervisor.isNotBlank() && currentSupervisor.contains("_")) {
            return currentSupervisor.split("_").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        }
        if (superKey.isNotBlank()) {
            return superKey.split("_", "-").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        }

        return currentSupervisor
    }

    suspend fun completeFirstAccess(
        matricula: String,
        novaSenha: String,
        telefone: String,
        placa: String,
        venCnh: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "senha" to novaSenha,
                "telefone" to telefone,
                "placa" to placa,
                "ven-cnh" to venCnh,
                "mode" to 0L
            )
            colaboradoresCollection.document(matricula.trim())
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(
        matricula: String,
        novaSenha: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "senha" to novaSenha,
                "mode" to 0L
            )
            colaboradoresCollection.document(matricula.trim())
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        matricula: String,
        telefone: String,
        placa: String,
        venCnh: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "telefone" to telefone,
                "placa" to placa,
                "ven-cnh" to venCnh
            )
            colaboradoresCollection.document(matricula.trim())
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
