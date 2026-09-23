package br.com.meirelesefreitas.go.checkonline.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object FirstAccess : Screen("first_access/{matricula}/{mode}") {
        fun createRoute(matricula: String, mode: Long): String = "first_access/$matricula/$mode"
    }
    object Main : Screen("main")
    object Checklist : Screen("checklist")
}
