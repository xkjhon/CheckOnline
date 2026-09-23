package br.com.meirelesefreitas.go.checkonline.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.com.meirelesefreitas.go.checkonline.ui.components.BottomNavDestination
import br.com.meirelesefreitas.go.checkonline.ui.components.ExpressiveNavBar
import br.com.meirelesefreitas.go.checkonline.ui.screens.history.HistoryScreen
import br.com.meirelesefreitas.go.checkonline.ui.screens.home.HomeScreen
import br.com.meirelesefreitas.go.checkonline.ui.screens.profile.ProfileScreen

@Composable
fun MainAppScreen(
    onNavigateToChecklist: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavDestination.Home.route) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ExpressiveNavBar(
                currentRoute = selectedTab,
                onNavigate = { route -> selectedTab = route }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 200),
                label = "tabCrossfade"
            ) { tab ->
                when (tab) {
                    BottomNavDestination.Home.route -> {
                        HomeScreen(
                            onNavigateToChecklist = onNavigateToChecklist,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    BottomNavDestination.History.route -> {
                        HistoryScreen(
                            snackbarHostState = snackbarHostState
                        )
                    }
                    BottomNavDestination.Profile.route -> {
                        ProfileScreen(
                            onLogout = onLogout,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}
