package com.example.gerenciador.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gerenciador.data.model.Task
import com.example.gerenciador.data.model.TaskStatus
import com.example.gerenciador.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val repository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: Long = checkNotNull(savedStateHandle["projectId"])
    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L

    val isEditMode = (taskId != -1L)

    private val _taskUiState = MutableStateFlow(TaskUiState())
    val taskUiState: StateFlow<TaskUiState> = _taskUiState.asStateFlow()

    // Job da coroutine do timer
    private var timerJob: Job? = null

    init {
        if (isEditMode) {
            loadTaskDetails()
        }
    }

    private fun loadTaskDetails() {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                _taskUiState.update {
                    it.copy(
                        titulo = task.titulo,
                        descricao = task.descricao,
                        status = task.status,
                        tempoTrabalhado = task.tempoTrabalhado,
                        timerAtivo = false,
                        ultimoInicioTimer = 0L,
                        taskOriginal = task
                    )
                }

            }
        }
    }

    fun onTituloChange(titulo: String) {
        _taskUiState.update { it.copy(titulo = titulo) }
    }

    fun onDescricaoChange(descricao: String) {
        _taskUiState.update { it.copy(descricao = descricao) }
    }

    fun onStatusChange(status: TaskStatus) {
        _taskUiState.update { it.copy(status = status) }
    }

    // Funções do timer
    fun toggleTimer() {
        val state = _taskUiState.value

        if (state.timerAtivo) {
            // Pausar
            pauseTimer()
        } else {
            // Iniciar
            startTimer()
        }
    }

    private fun startTimer() {
        _taskUiState.update {
            it.copy(
                timerAtivo = true,
                ultimoInicioTimer = System.currentTimeMillis()
            )
        }

        // Inicia a coroutine do timer
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Atualiza a cada 1 segundo

                val state = _taskUiState.value
                val tempoDecorrido = System.currentTimeMillis() - state.ultimoInicioTimer

                _taskUiState.update {
                    it.copy(
                        tempoTrabalhadoAtual = state.tempoTrabalhado + tempoDecorrido
                    )
                }
            }
        }

        // Salva no banco que o timer está ativo
        saveTimerState()
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null

        val state = _taskUiState.value
        val tempoDecorrido = System.currentTimeMillis() - state.ultimoInicioTimer
        val novoTempoTotal = state.tempoTrabalhado + tempoDecorrido

        _taskUiState.update {
            it.copy(
                timerAtivo = false,
                tempoTrabalhado = novoTempoTotal,
                tempoTrabalhadoAtual = novoTempoTotal,
                ultimoInicioTimer = 0L
            )
        }

        // Salva o tempo acumulado no banco
        saveTimerState()
    }

    // Pausar o timer e sair
    fun pauseTimerOnExit() {
        if (_taskUiState.value.timerAtivo) {
            pauseTimer()
        }
    }

    private fun saveTimerState() {
        viewModelScope.launch {
            val state = _taskUiState.value
            if (isEditMode && state.taskOriginal != null) {
                val taskAtualizada = state.taskOriginal.copy(
                    tempoTrabalhado = state.tempoTrabalhado,
                    timerAtivo = state.timerAtivo,
                    ultimoInicioTimer = state.ultimoInicioTimer
                )
                repository.updateTask(taskAtualizada)
            }
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            val state = _taskUiState.value

            // Se o timer estava ativo, pausa antes de salvar
            if (state.timerAtivo) {
                pauseTimer()
            }

            if (isEditMode && state.taskOriginal != null) {
                val taskAtualizada = state.taskOriginal.copy(
                    titulo = state.titulo,
                    descricao = state.descricao,
                    status = state.status,
                    tempoTrabalhado = state.tempoTrabalhado,
                    timerAtivo = false,
                    ultimoInicioTimer = 0L
                )
                repository.updateTask(taskAtualizada)
            } else {
                val novaTask = Task(
                    projectId = projectId,
                    titulo = state.titulo,
                    descricao = state.descricao,
                    status = state.status,
                    tempoTrabalhado = state.tempoTrabalhado
                )
                repository.insertTask(novaTask)
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            timerJob?.cancel() // Cancela o timer se estiver ativo
            val state = _taskUiState.value
            if (isEditMode && state.taskOriginal != null) {
                repository.deleteTask(state.taskOriginal)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel() // Limpa o timer ao destruir o ViewModel
    }
}

data class TaskUiState(
    val titulo: String = "",
    val descricao: String = "",
    val status: TaskStatus = TaskStatus.PENDENTE,
    val tempoTrabalhado: Long = 0L,
    val timerAtivo: Boolean = false,
    val ultimoInicioTimer: Long = 0L,
    val tempoTrabalhadoAtual: Long = 0L, // Para exibir em tempo real
    val taskOriginal: Task? = null
)