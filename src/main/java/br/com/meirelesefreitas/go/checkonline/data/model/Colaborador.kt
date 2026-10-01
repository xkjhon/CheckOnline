package br.com.meirelesefreitas.go.checkonline.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName

enum class ColaboradorAct(val code: String, val title: String, val description: String) {
    AGENTE_COMERCIAL("agcom", "Agente Comercial", "Vistoria Comercial & Equipamentos"),
    LEITURISTA_PEDESTRE("leitpe", "Leiturista Pedestre", "Vistoria de Campo Pedestre"),
    LEITURISTA_MOTORISTA("leitmo", "Leiturista Motorista", "Vistoria de Campo & Veículo");

    companion object {
        fun fromCode(code: String?): ColaboradorAct? {
            val clean = code?.trim()?.lowercase() ?: return null
            return entries.find { it.code == clean }
        }
    }
}

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
    val mode: Long = 0L, // 0 = Ativo, 1 = Primeiro Acesso pendente, 2 = Restaurar Senha pendente
    val act: String = "" // "agcom" = Agente Comercial, "leitpe" = Leiturista Pedestre, "leitmo" = Leiturista Motorista
) {
    val actEnum: ColaboradorAct?
        get() = ColaboradorAct.fromCode(act)

    val funcaoDescricao: String
        get() = actEnum?.title ?: if (act.isNotBlank()) act else "Agente Comercial"

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
            val act = doc.getString("act") ?: ""

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
                mode = mode,
                act = act
            )
        }
    }
}
