package br.com.meirelesefreitas.go.checkonline.ui.screens.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.meirelesefreitas.go.checkonline.ui.components.ExpressiveCard
import br.com.meirelesefreitas.go.checkonline.ui.components.HomeScreenSkeleton
import br.com.meirelesefreitas.go.checkonline.ui.theme.ExpressiveShapes
import br.com.meirelesefreitas.go.checkonline.utils.DateUtils

@Composable
fun HomeScreen(
    onNavigateToChecklist: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(viewModel.snackbarEvent) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState?.showSnackbar(message)
        }
    }

    if (uiState.isLoading && uiState.colaborador == null) {
        HomeScreenSkeleton()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Header: "CheckOnline"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CheckOnline",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    if (uiState.colaborador != null) {
                        Text(
                            text = "Olá, ${uiState.colaborador?.nome?.split(" ")?.firstOrNull() ?: "Colaborador"} • ${uiState.colaborador?.localidade}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Online",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= BANNER 1: Card Principal do Ponto =================
            ExpressiveCard(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REGISTRO DE PONTO & VISTORIA",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horário em tempo real HH:mm:ss
                    Text(
                        text = uiState.currentTimeString.ifEmpty { "00:00:00" },
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Data atual por extenso
                    Text(
                        text = uiState.currentDateFullString.ifEmpty { "Carregando data..." },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Badge se já respondeu
                    if (uiState.hasAnsweredToday) {
                        val badgeBg = if (isDark) Color(0xFF0E381C) else Color(0xFFCEEAD6)
                        val badgeText = if (isDark) Color(0xFF8CECA8) else Color(0xFF0D5A22)

                        Surface(
                            shape = ExpressiveShapes.medium,
                            color = badgeBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badgeText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Checklist de hoje já realizado ✓",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = badgeText
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Botão tonal "CheckList >"
                    Button(
                        onClick = onNavigateToChecklist,
                        enabled = !uiState.hasAnsweredToday,
                        shape = ExpressiveShapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (uiState.hasAnsweredToday) "Checklist Concluído Hoje" else "CheckList",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= BANNER 2: Dias Pendentes (Alto Contraste no Dark Mode) =================
            val hasPending = uiState.pendingBusinessDays.isNotEmpty()
            val pendingContainerBg = when {
                hasPending && isDark -> Color(0xFF3B2500)
                hasPending && !isDark -> Color(0xFFFFECC0)
                !hasPending && isDark -> Color(0xFF0F361C)
                else -> Color(0xFFCEEAD6)
            }
            val pendingTitleColor = when {
                hasPending && isDark -> Color(0xFFFFD180)
                hasPending && !isDark -> Color(0xFF6B3D00)
                !hasPending && isDark -> Color(0xFF90F2B0)
                else -> Color(0xFF0D5323)
            }
            val pendingBodyColor = when {
                hasPending && isDark -> Color(0xFFFFECCC)
                hasPending && !isDark -> Color(0xFF382200)
                !hasPending && isDark -> Color(0xFFE2E3DF)
                else -> Color(0xFF191C1A)
            }
            val pendingSubtextColor = when {
                hasPending && isDark -> Color(0xFFDFCCAA)
                hasPending && !isDark -> Color(0xFF5A3E1E)
                !hasPending && isDark -> Color(0xFFC1C9C0)
                else -> Color(0xFF414942)
            }

            ExpressiveCard(
                containerColor = pendingContainerBg
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasPending) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = pendingTitleColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasPending) {
                                "${uiState.pendingBusinessDays.size} Dia(s) Pendente(s) no Mês"
                            } else {
                                "Nenhum dia pendente no mês"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = pendingTitleColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (hasPending) {
                        val daysFormatted = uiState.pendingBusinessDays.take(5).joinToString(", ") { DateUtils.formatLocalDate(it) }
                        val moreText = if (uiState.pendingBusinessDays.size > 5) " e mais ${uiState.pendingBusinessDays.size - 5} dia(s)" else ""

                        Text(
                            text = "Dias sem registro: $daysFormatted$moreText.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = pendingBodyColor
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Não é permitido preenchimento retroativo. Procure o supervisor imediato para justificativa.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = pendingSubtextColor
                            )
                        )
                    } else {
                        Text(
                            text = "Excelente! Todos os dias úteis anteriores deste mês foram registrados em campo.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = pendingSubtextColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= BANNER 3: Sincronização Offline =================
            val hasLongSyncDelay = uiState.daysWithoutSync >= 2
            val syncContainerBg = when {
                hasLongSyncDelay && isDark -> Color(0xFF491111)
                hasLongSyncDelay && !isDark -> Color(0xFFFAD2CF)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val syncTitleColor = when {
                hasLongSyncDelay && isDark -> Color(0xFFFFB4AB)
                hasLongSyncDelay && !isDark -> Color(0xFF93000A)
                else -> MaterialTheme.colorScheme.onSurface
            }

            ExpressiveCard(
                containerColor = syncContainerBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (hasLongSyncDelay) Icons.Default.WarningAmber else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (hasLongSyncDelay) syncTitleColor else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (hasLongSyncDelay) {
                                    "${uiState.daysWithoutSync} Dias sem sincronização"
                                } else {
                                    "Sincronização Offline"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = syncTitleColor
                                )
                            )
                            Text(
                                text = if (hasLongSyncDelay) {
                                    "Dados acumulados no aparelho"
                                } else {
                                    "Base pronta para uso offline"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = viewModel::synchronize,
                        enabled = !uiState.isSyncing,
                        shape = ExpressiveShapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasLongSyncDelay) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sincronizar",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
