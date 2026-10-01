package br.com.meirelesefreitas.go.checkonline.data.model

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class ChecklistDoc(
    val id: String = "",
    val data: Date? = null,
    val observacao: String = "",
    val matricula: String = "",
    val localidade: String = "",
    val answers: Map<Int, Int> = emptyMap(), // Map<QuestionId, AnswerCode (1=Sim, 2=Não, 3=N/A)>
    val isPendingSync: Boolean = false // Indica se foi gravado offline e aguarda envio ao Firestore
) {
    val totalSim: Int
        get() = answers.values.count { it == ChecklistAnswer.SIM.code }

    val totalNao: Int
        get() = answers.values.count { it == ChecklistAnswer.NAO.code }

    val totalNa: Int
        get() = answers.values.count { it == ChecklistAnswer.NAO_APLICA.code }

    val hasInconformidade: Boolean
        get() = totalNao > 0

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ChecklistDoc {
            val id = doc.id
            val timestamp = doc.getTimestamp("data")
            val date = timestamp?.toDate()
            val observacao = doc.getString("observacao") ?: ""
            val matricula = doc.getString("matricula") ?: ""
            val localidade = doc.getString("cidade") ?: doc.getString("localidade") ?: ""

            val answersMap = mutableMapOf<Int, Int>()
            val dataMap = doc.data
            if (dataMap != null) {
                for ((key, value) in dataMap) {
                    val questionNum = key.toIntOrNull()
                    if (questionNum != null) {
                        answersMap[questionNum] = ChecklistAnswer.fromAny(value).code
                    }
                }
            }

            return ChecklistDoc(
                id = id,
                data = date,
                observacao = observacao,
                matricula = matricula,
                localidade = localidade,
                answers = answersMap,
                isPendingSync = false
            )
        }
    }
}
