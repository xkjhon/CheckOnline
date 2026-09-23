package br.com.meirelesefreitas.go.checkonline.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.meirelesefreitas.go.checkonline.ui.components.ExpressiveCard
import br.com.meirelesefreitas.go.checkonline.ui.theme.ExpressiveError
import br.com.meirelesefreitas.go.checkonline.ui.theme.ExpressiveErrorContainer
import br.com.meirelesefreitas.go.checkonline.ui.theme.ExpressiveShapes

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.navEvent) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is ProfileNavEvent.LoggedOut -> onLogout()
                is ProfileNavEvent.ShowMessage -> snackbarHostState?.showSnackbar(event.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Perfil do Usuário",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Informações cadastrais e configurações",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Identity Card
        ExpressiveCard(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = uiState.colaborador?.nome?.ifEmpty { "Colaborador" } ?: "Colaborador",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Matrícula: ${uiState.colaborador?.matricula ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Cidade: ${uiState.colaborador?.localidade ?: "-"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Information Details Card
        ExpressiveCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Dados Operacionais",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProfileInfoRow(
                    icon = Icons.Default.SupervisorAccount,
                    label = "Supervisor",
                    value = uiState.colaborador?.supervisor?.ifEmpty { "-" } ?: "-"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Telefone / WhatsApp",
                    value = uiState.colaborador?.telefone?.ifEmpty { "Não cadastrado" } ?: "Não cadastrado"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileInfoRow(
                    icon = Icons.Default.DirectionsCar,
                    label = "Placa do Veículo",
                    value = uiState.colaborador?.placa?.ifEmpty { "Não informada" } ?: "Não informada"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Vencimento da CNH",
                    value = uiState.colaborador?.venCnh?.ifEmpty { "Não informado" } ?: "Não informado"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action: Editar Usuário
        OutlinedButton(
            onClick = viewModel::openEditDialog,
            shape = ExpressiveShapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Editar Dados Operacionais",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botão tonal de alerta "Sair da Sessão"
        Button(
            onClick = viewModel::logout,
            shape = ExpressiveShapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = ExpressiveErrorContainer,
                contentColor = ExpressiveError
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = ExpressiveError,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sair da Sessão",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ExpressiveError
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Edit Profile Dialog
    if (uiState.isEditDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::dismissEditDialog,
            title = {
                Text(
                    text = "Editar Dados do Colaborador",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Atualize seu telefone, placa ou vencimento de CNH.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.editTelefone,
                        onValueChange = viewModel::onEditTelefoneChanged,
                        label = { Text("Telefone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = ExpressiveShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.editPlaca,
                        onValueChange = viewModel::onEditPlacaChanged,
                        label = { Text("Placa do Veículo") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        singleLine = true,
                        shape = ExpressiveShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.editVenCnh,
                        onValueChange = viewModel::onEditVenCnhChanged,
                        label = { Text("Vencimento CNH (dd/mm/aaaa)") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        singleLine = true,
                        shape = ExpressiveShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveProfile,
                    enabled = !uiState.isSaving,
                    shape = ExpressiveShapes.small
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Salvar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissEditDialog) {
                    Text("Cancelar")
                }
            },
            shape = ExpressiveShapes.large
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
