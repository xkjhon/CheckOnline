package br.com.meirelesefreitas.go.checkonline.utils

data class ChecklistSection(
    val sectionNumber: Int,
    val title: String,
    val subtitle: String = "",
    val questions: List<ChecklistQuestion>
)

data class ChecklistQuestion(
    val id: Int,
    val title: String,
    val category: String,
    val subtitle: String = "",
    val sectionNumber: Int = 1,
    val numberInSection: Int = id
)

object CheckListItems {

    // ==========================================
    // PERGUNTAS AGENTE COMERCIAL ("agcom")
    // ==========================================

    // SEÇÃO 1: MONITORAMENTO DO CELULAR/POS
    val agcomSecao1 = listOf(
        ChecklistQuestion(
            id = 1,
            title = "Celular",
            category = "Monitoramento do Celular/POS",
            subtitle = "Aparelho celular corporativo e carregador operacional",
            sectionNumber = 1,
            numberInSection = 1
        ),
        ChecklistQuestion(
            id = 2,
            title = "Maquininha de Cartão POS",
            category = "Monitoramento do Celular/POS",
            subtitle = "Equipamento POS para recebimentos em campo",
            sectionNumber = 1,
            numberInSection = 2
        )
    )

    // SEÇÃO 2: EPI - EQUIPAMENTOS DE PROTEÇÃO INDIVIDUAL
    val agcomSecao2 = listOf(
        ChecklistQuestion(
            id = 3,
            title = "Calça",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Uniforme padrão da empresa, limpa e sem rasgos",
            sectionNumber = 2,
            numberInSection = 1
        ),
        ChecklistQuestion(
            id = 4,
            title = "Camisa",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Camisa padrão da empresa limpa e íntegra",
            sectionNumber = 2,
            numberInSection = 2
        ),
        ChecklistQuestion(
            id = 5,
            title = "Crachá",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Identificação funcional visível com foto e dados",
            sectionNumber = 2,
            numberInSection = 3
        ),
        ChecklistQuestion(
            id = 6,
            title = "Óculos de Segurança",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Lentes limpas, sem trincas ou riscos severos",
            sectionNumber = 2,
            numberInSection = 4
        ),
        ChecklistQuestion(
            id = 7,
            title = "Calçado de Segurança",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Botina de segurança com biqueira e solado adequado",
            sectionNumber = 2,
            numberInSection = 5
        ),
        ChecklistQuestion(
            id = 8,
            title = "Luva de Vaqueta",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Luva para manuseio e proteção das mãos",
            sectionNumber = 2,
            numberInSection = 6
        ),
        ChecklistQuestion(
            id = 9,
            title = "Luva de Pilotagem",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Luvas para condução de motocicleta",
            sectionNumber = 2,
            numberInSection = 7
        ),
        ChecklistQuestion(
            id = 10,
            title = "Joelheira",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Joelheiras de proteção para pilotagem",
            sectionNumber = 2,
            numberInSection = 8
        ),
        ChecklistQuestion(
            id = 11,
            title = "Protetor Solar",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Prevenção e proteção contra radiação solar",
            sectionNumber = 2,
            numberInSection = 9
        ),
        ChecklistQuestion(
            id = 12,
            title = "Detector de Tensão",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Aparelho detector testado e operacional",
            sectionNumber = 2,
            numberInSection = 10
        ),
        ChecklistQuestion(
            id = 13,
            title = "Protetor de Tórax",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Colete / protetor torácico ajustado",
            sectionNumber = 2,
            numberInSection = 11
        ),
        ChecklistQuestion(
            id = 14,
            title = "Capacete Motociclista",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Viseira limpa, fecho jugular e dentro da validade",
            sectionNumber = 2,
            numberInSection = 12
        ),
        ChecklistQuestion(
            id = 15,
            title = "Cotoveleira",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Cotoveleiras de proteção para pilotagem",
            sectionNumber = 2,
            numberInSection = 13
        ),
        ChecklistQuestion(
            id = 16,
            title = "Capa de Chuva",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Capa de chuva completa e impermeável",
            sectionNumber = 2,
            numberInSection = 14
        ),
        ChecklistQuestion(
            id = 17,
            title = "Garrafa Térmica",
            category = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Garrafa para hidratação adequada em rota",
            sectionNumber = 2,
            numberInSection = 15
        )
    )

    // SEÇÃO 3: ITENS DA VIATURA/MOTO
    val agcomSecao3 = listOf(
        ChecklistQuestion(
            id = 18,
            title = "KM Atual",
            category = "Itens da Viatura/Moto",
            subtitle = "Odômetro conferido e registrado",
            sectionNumber = 3,
            numberInSection = 1
        ),
        ChecklistQuestion(
            id = 19,
            title = "Doc. do Veículo Licenciamento/DUT/IPVA",
            category = "Itens da Viatura/Moto",
            subtitle = "Documentação em dia e porte obrigatório",
            sectionNumber = 3,
            numberInSection = 2
        ),
        ChecklistQuestion(
            id = 20,
            title = "Condições do Pneu",
            category = "Itens da Viatura/Moto",
            subtitle = "Calibragem, desgaste e marcas TWI em dia",
            sectionNumber = 3,
            numberInSection = 3
        ),
        ChecklistQuestion(
            id = 21,
            title = "Arco Corta Pipa (Anti Cerol)",
            category = "Itens da Viatura/Moto",
            subtitle = "Antena de proteção instalada e estendida",
            sectionNumber = 3,
            numberInSection = 4
        ),
        ChecklistQuestion(
            id = 22,
            title = "Luz da Seta (Dir. e Esq.)",
            category = "Itens da Viatura/Moto",
            subtitle = "Sinalizadores de direção dianteiros e traseiros",
            sectionNumber = 3,
            numberInSection = 5
        ),
        ChecklistQuestion(
            id = 23,
            title = "Luz do Farol",
            category = "Itens da Viatura/Moto",
            subtitle = "Farol alto e baixo operacionais",
            sectionNumber = 3,
            numberInSection = 6
        ),
        ChecklistQuestion(
            id = 24,
            title = "Luz da Placa",
            category = "Itens da Viatura/Moto",
            subtitle = "Iluminação traseira da placa funcionando",
            sectionNumber = 3,
            numberInSection = 7
        ),
        ChecklistQuestion(
            id = 25,
            title = "Luz do Freio",
            category = "Itens da Viatura/Moto",
            subtitle = "Acionamento da luz ao frear manete e pedal",
            sectionNumber = 3,
            numberInSection = 8
        ),
        ChecklistQuestion(
            id = 26,
            title = "Retrovisor (Dir. e Esq.)",
            category = "Itens da Viatura/Moto",
            subtitle = "Ambos os retrovisores fixos, limpos e regulados",
            sectionNumber = 3,
            numberInSection = 9
        ),
        ChecklistQuestion(
            id = 27,
            title = "Kit Relação",
            category = "Itens da Viatura/Moto",
            subtitle = "Corrente, coroa, pinhão e lubrificação",
            sectionNumber = 3,
            numberInSection = 10
        ),
        ChecklistQuestion(
            id = 28,
            title = "Buzina",
            category = "Itens da Viatura/Moto",
            subtitle = "Sinal sonoro de alerta operacional",
            sectionNumber = 3,
            numberInSection = 11
        ),
        ChecklistQuestion(
            id = 29,
            title = "Faixa de Sinalização para Baú",
            category = "Itens da Viatura/Moto",
            subtitle = "Fita refletiva regulamentar aplicada e limpa",
            sectionNumber = 3,
            numberInSection = 12
        ),
        ChecklistQuestion(
            id = 30,
            title = "Logotipo da Empresa Contratante/Contratada",
            category = "Itens da Viatura/Moto",
            subtitle = "Identificação visual visível e preservada",
            sectionNumber = 3,
            numberInSection = 13
        ),
        ChecklistQuestion(
            id = 31,
            title = "Freio Pedal",
            category = "Itens da Viatura/Moto",
            subtitle = "Freio traseiro com resposta e altura corretas",
            sectionNumber = 3,
            numberInSection = 14
        ),
        ChecklistQuestion(
            id = 32,
            title = "Freio Manual",
            category = "Itens da Viatura/Moto",
            subtitle = "Manete de freio dianteiro firme e eficiente",
            sectionNumber = 3,
            numberInSection = 15
        ),
        ChecklistQuestion(
            id = 33,
            title = "Grade de Proteção Frontal (Mata-Cachorro)",
            category = "Itens da Viatura/Moto",
            subtitle = "Protetor frontal fixo e íntegro",
            sectionNumber = 3,
            numberInSection = 16
        ),
        ChecklistQuestion(
            id = 34,
            title = "Manete (Esq. e Dir.)",
            category = "Itens da Viatura/Moto",
            subtitle = "Manetes sem folgas anormais ou trincas",
            sectionNumber = 3,
            numberInSection = 17
        ),
        ChecklistQuestion(
            id = 35,
            title = "Assento",
            category = "Itens da Viatura/Moto",
            subtitle = "Banco travado, sem rasgos ou espuma exposta",
            sectionNumber = 3,
            numberInSection = 18
        ),
        ChecklistQuestion(
            id = 36,
            title = "Nivel do Oléo",
            category = "Itens da Viatura/Moto",
            subtitle = "Nível do óleo do motor entre mínimo e máximo",
            sectionNumber = 3,
            numberInSection = 19
        )
    )

    val agcomSections: List<ChecklistSection> = listOf(
        ChecklistSection(
            sectionNumber = 1,
            title = "Monitoramento do Celular/POS",
            subtitle = "Dispositivos Móveis e POS",
            questions = agcomSecao1
        ),
        ChecklistSection(
            sectionNumber = 2,
            title = "EPI - Equipamentos de Proteção Individual",
            subtitle = "Equipamentos de Segurança Individual",
            questions = agcomSecao2
        ),
        ChecklistSection(
            sectionNumber = 3,
            title = "Itens da Viatura/Moto",
            subtitle = "Segurança e Condições da Viatura",
            questions = agcomSecao3
        )
    )

    val agcomQuestions: List<ChecklistQuestion> = agcomSecao1 + agcomSecao2 + agcomSecao3

    // Para leituristas (por enquanto não terá)
    val leitpeQuestions: List<ChecklistQuestion> = emptyList()
    val leitmoQuestions: List<ChecklistQuestion> = emptyList()

    val leitpeSections: List<ChecklistSection> = emptyList()
    val leitmoSections: List<ChecklistSection> = emptyList()

    // Retorna todos os itens padrão (mantendo compatibilidade com referências a CheckListItems.items)
    val items: List<ChecklistQuestion>
        get() = agcomQuestions

    fun getQuestionsForAct(act: String?): List<ChecklistQuestion> {
        val cleanAct = act?.trim()?.lowercase() ?: ""
        return when (cleanAct) {
            "agcom" -> agcomQuestions
            "leitpe" -> leitpeQuestions
            "leitmo" -> leitmoQuestions
            else -> agcomQuestions // Default fallback quando act não estiver preenchido ou for agcom
        }
    }

    fun getSectionsForAct(act: String?): List<ChecklistSection> {
        val cleanAct = act?.trim()?.lowercase() ?: ""
        return when (cleanAct) {
            "agcom" -> agcomSections
            "leitpe" -> leitpeSections
            "leitmo" -> leitmoSections
            else -> agcomSections
        }
    }

    fun getQuestionById(id: Int, act: String? = null): ChecklistQuestion? {
        val list = if (!act.isNullOrBlank()) getQuestionsForAct(act) else agcomQuestions
        return list.find { it.id == id } ?: agcomQuestions.find { it.id == id }
    }
}
