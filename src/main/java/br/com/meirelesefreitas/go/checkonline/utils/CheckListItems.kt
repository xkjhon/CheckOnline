package br.com.meirelesefreitas.go.checkonline.utils

data class ChecklistQuestion(
    val id: Int,
    val title: String,
    val category: String,
    val subtitle: String = ""
)

object CheckListItems {
    val items: List<ChecklistQuestion> = listOf(
        ChecklistQuestion(
            id = 1,
            title = "Celular",
            category = "Dispositivos Eletrônicos",
            subtitle = "Monitoramento do Celular/POS"
        ),
        ChecklistQuestion(
            id = 2,
            title = "Máquina de Cartão (POS)",
            category = "Dispositivos Eletrônicos",
            subtitle = "Monitoramento do Celular/POS"
        )
    )
}
