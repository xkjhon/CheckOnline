package br.com.meirelesefreitas.go.checkonline.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class ChecklistDoc(
    val id: String = "",
    val data: Date? = null,
    val observacao: String = "",
    val matricula: String = "",
    val localidade: String = "",
    val answers: Map<Int, Boolean> = emptyMap()
) {
    val totalSim: Int
        get() = answers.values.count { it }

    val totalNao: Int
        get() = answers.values.count { !it }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ChecklistDoc {
            val id = doc.id
            val timestamp = doc.getTimestamp("data")
            val date = timestamp?.toDate()
            val observacao = doc.getString("observacao") ?: ""
            val matricula = doc.getString("matricula") ?: ""
            val localidade = doc.getString("cidade") ?: doc.getString("localidade") ?: ""

            val answersMap = mutableMapOf<Int, Boolean>()
            val dataMap = doc.data
            if (dataMap != null) {
                for ((key, value) in dataMap) {
                    val questionNum = key.toIntOrNull()
                    if (questionNum != null && value is Boolean) {
                        answersMap[questionNum] = value
                    }
                }
            }

            return ChecklistDoc(
                id = id,
                data = date,
                observacao = observacao,
                matricula = matricula,
                localidade = localidade,
                answers = answersMap
            )
        }
    }
}
