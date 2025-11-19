package com.example.gerenciador.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gerenciador.data.model.TaskStatus
import com.example.gerenciador.presentation.viewmodel.TaskEditViewModel
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    navController: NavController,
    viewModel: TaskEditViewModel = hiltViewModel()
) {
    val taskUiState by viewModel.taskUiState.collectAsState()

    // Pausa o timer quando sair da tela
    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseTimerOnExit()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Editar Tarefa" else "Nova Tarefa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    // Botão de deletar (apenas no modo edição)
                    if (viewModel.isEditMode) {
                        IconButton(
                            onClick = {
                                viewModel.deleteTask()
                                navController.popBackStack()
                            }
                        ) {
                            Icon(Icons.Default.Delete, "Excluir Tarefa")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveTask()
                    navController.popBackStack()
                },
                icon = { Icon(Icons.Default.CheckCircle, "Salvar") },
                text = { Text("Salvar Tarefa") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // SEÇÃO DE STATUS E CONCLUÍDA
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Status da Tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botão para marcar como concluída
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newStatus = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                                    TaskStatus.PENDENTE
                                } else {
                                    TaskStatus.CONCLUIDA
                                }
                                viewModel.onStatusChange(newStatus)
                            }
                    ) {
                        Icon(
                            imageVector = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = "Status",
                            tint = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                                    "Tarefa Concluída"
                                } else {
                                    "Marcar como Concluída"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )

                            Text(
                                text = if (taskUiState.status == TaskStatus.CONCLUIDA) {
                                    "Esta tarefa está marcada como concluída"
                                } else {
                                    "Clique para marcar esta tarefa como concluída"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status atual
                    Text(
                        text = "Status: ${taskUiState.status.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SEÇÃO DO TIMER (apenas no modo edição)
            if (viewModel.isEditMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Timer de Trabalho",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tempo trabalhado
                        Text(
                            text = "Tempo trabalhado: ${formatTime(taskUiState.tempoTrabalhadoAtual)}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botão Play/Pause
                        Button(
                            onClick = { viewModel.toggleTimer() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (taskUiState.timerAtivo) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = "Timer"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (taskUiState.timerAtivo) "Pausar Timer" else "Iniciar Timer")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // FORMULÁRIO DA TAREFA
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detalhes da Tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = taskUiState.titulo,
                        onValueChange = { viewModel.onTituloChange(it) },
                        label = { Text("Título da Tarefa *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = taskUiState.titulo.isBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = taskUiState.descricao,
                        onValueChange = { viewModel.onDescricaoChange(it) },
                        label = { Text("Descrição") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            }
        }
    }
}

/**
 * Formata o tempo de milissegundos para formato legível (HH:MM:SS)
 */
private fun formatTime(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}