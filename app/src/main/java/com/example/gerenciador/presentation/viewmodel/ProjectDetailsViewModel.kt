package com.example.gerenciador.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gerenciador.data.model.Project
import com.example.gerenciador.data.model.Task
import com.example.gerenciador.data.model.TaskStatus
import com.example.gerenciador.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val repository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navProjectId: Long = savedStateHandle.get<Long>("projectId") ?: 0L

    // States existentes do projeto
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showGitHubImportDialog = MutableStateFlow(false)
    val showGitHubImportDialog: StateFlow<Boolean> = _showGitHubImportDialog.asStateFlow()

    private val _githubImportState = MutableStateFlow(GitHubImportState())
    val githubImportState: StateFlow<GitHubImportState> = _githubImportState.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    init {
        if (navProjectId != 0L) {
            loadProjectDetails()
            loadTasks()
        } else {
            _errorMessage.update { "Erro fatal: ID do projeto não recebido." }
        }
    }

    // FUNÇÕES DO GITHUB IMPORT
    fun onGitHubOwnerChange(owner: String) {
        _githubImportState.update { it.copy(owner = owner) }
    }

    fun onGitHubRepoChange(repo: String) {
        _githubImportState.update { it.copy(repo = repo) }
    }

    fun openGitHubImportDialog() {
        _showGitHubImportDialog.value = true
        _errorMessage.value = null
    }

    fun closeGitHubImportDialog() {
        _showGitHubImportDialog.value = false
        _githubImportState.value = GitHubImportState()
    }

    fun importGitHubIssues() {
        val state = _githubImportState.value

        if (state.owner.isBlank() || state.repo.isBlank()) {
            _errorMessage.value = "Preencha Dono e Repositório."
            return
        }

        viewModelScope.launch {
            _isImporting.value = true
            _errorMessage.value = null

            try {
                val result = repository.importIssuesFromGitHub(
                    owner = state.owner,
                    repo = state.repo,
                    projectId = navProjectId
                )

                _errorMessage.value = "${result.getOrThrow()} tarefas importadas!"
                closeGitHubImportDialog()

            } catch (e: Exception) {
                _errorMessage.value = "Erro ao importar: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    // Marcar/desmarcar tarefa
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            try {
                val newStatus = when (task.status) {
                    TaskStatus.CONCLUIDA -> TaskStatus.PENDENTE
                    else -> TaskStatus.CONCLUIDA
                }

                repository.updateTaskStatus(task.id, newStatus)
                // O Flow vai atualizar automaticamente a lista

            } catch (e: Exception) {
                _errorMessage.value = "Erro ao atualizar tarefa: ${e.message}"
            }
        }
    }

    // FUNÇÕES EXISTENTES
    private fun loadProjectDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val projectDetails = repository.getProjectById(navProjectId)
                _project.value = projectDetails
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar projeto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTasks() {
        repository.getTasksByProject(navProjectId)
            .onEach { taskList ->
                _tasks.update { taskList }
            }
            .launchIn(viewModelScope)
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Data class para estado do formulário GitHub
    data class GitHubImportState(
        val owner: String = "",
        val repo: String = ""
    )
}