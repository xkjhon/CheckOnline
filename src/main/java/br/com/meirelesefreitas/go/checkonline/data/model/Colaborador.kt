package br.com.meirelesefreitas.go.checkonline.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName

data class Colaborador(
    val matricula: String = "",
    val nome: String = "",
    val localidade: String = "",
    val superRegiao: String = "", // field "super", "supervisao", "regional"
    val supervisor: String = "",
    val telefone: String = "",
    val placa: String = "",
    @get:PropertyName("ven-cnh")
    @set:PropertyName("ven-cnh")
    var venCnh: String = "",
    val senha: String = "",
    val mode: Long = 0L // 0 = Ativo, 1 = Primeiro Acesso pendente, 2 = Restaurar Senha pendente
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Colaborador? {
            if (!doc.exists()) return null
            val matricula = doc.id
            val nome = doc.getString("nome") ?: ""
            val localidade = doc.getString("localidade") ?: doc.getString("cidade") ?: ""
            val superRegiao = doc.getString("super") ?: doc.getString("supervisao") ?: doc.getString("regional") ?: ""
            val supervisor = doc.getString("supervisor")
                ?: doc.getString("nome_supervisor")
                ?: doc.getString("super_nome")
                ?: doc.getString("supervisor_nome")
                ?: ""
            val telefone = doc.getString("telefone") ?: ""
            val placa = doc.getString("placa") ?: ""
            val venCnh = doc.getString("ven-cnh") ?: doc.getString("venCnh") ?: ""
            val senha = doc.getString("senha") ?: ""
            val mode = doc.getLong("mode") ?: 0L

            return Colaborador(
                matricula = matricula,
                nome = nome,
                localidade = localidade,
                superRegiao = superRegiao,
                supervisor = supervisor,
                telefone = telefone,
                placa = placa,
                venCnh = venCnh,
                senha = senha,
                mode = mode
            )
        }
    }
}
