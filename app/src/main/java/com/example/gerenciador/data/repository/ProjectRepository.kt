package com.example.gerenciador.data.repository

import com.example.gerenciador.data.dao.ProjectDao
import com.example.gerenciador.data.dao.TaskDao
import com.example.gerenciador.data.model.Project
import com.example.gerenciador.data.model.ProjectStatus
import com.example.gerenciador.data.model.Task
import com.example.gerenciador.data.model.TaskStatus
import com.example.gerenciador.data.remote.GitHubApi
import com.example.gerenciador.data.adapter.GitHubAdapter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao,
    private val gitHubApi: GitHubApi
) {

    // Métodos de projetos
    suspend fun insertProject(project: Project): Long {
        return projectDao.insert(project)
    }

    suspend fun updateProject(project: Project): Int {
        return projectDao.update(project)
    }

    suspend fun deleteProject(project: Project): Int {
        return projectDao.delete(project)
    }

    suspend fun getProjectById(id: Long): Project? {
        return projectDao.getById(id)
    }

    fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAll()
    }

    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>> {
        return projectDao.getByStatus(status.name)
    }

    fun getUpcomingDeadlines(): Flow<List<Project>> {
        return projectDao.getUpcomingDeadlines()
    }

    fun getProjectsByClient(clientName: String): Flow<List<Project>> {
        return projectDao.getByClient(clientName)
    }

    fun getProjectsCount(): Flow<Int> {
        return projectDao.getCount()
    }

    fun searchProjects(query: String): Flow<List<Project>> {
        return projectDao.getAll()
    }

    // Métodos das tasks
    suspend fun insertTask(task: Task): Long {
        return taskDao.insert(task)
    }

    suspend fun updateTask(task: Task): Int {
        return taskDao.update(task)
    }

    suspend fun deleteTask(task: Task): Int {
        return taskDao.delete(task)
    }

    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getById(id)
    }

    fun getTasksByProject(projectId: Long): Flow<List<Task>> {
        return taskDao.getByProject(projectId)
    }

    fun getTasksByStatus(projectId: Long, status: TaskStatus): Flow<List<Task>> {
        return taskDao.getByStatus(projectId, status.name)
    }

    fun getPendingTasks(): Flow<List<Task>> {
        return taskDao.getPendingTasks()
    }

    fun getTaskCountByProject(projectId: Long): Flow<Int> {
        return taskDao.getCountByProject(projectId)
    }

    fun getTaskCountByStatus(projectId: Long, status: TaskStatus): Flow<Int> {
        return taskDao.getCountByStatus(projectId, status.name)
    }

    suspend fun deleteTasksByProject(projectId: Long): Int {
        return taskDao.deleteByProject(projectId)
    }

    // Atualizar o status da tarefa
    suspend fun updateTaskStatus(taskId: Long, newStatus: TaskStatus) {
        taskDao.updateTaskStatus(taskId, newStatus.name)
    }

    // Métodos de integração com o github
    suspend fun importIssuesFromGitHub(owner: String, repo: String, projectId: Long): Result<Int> {
        return try {
            val response = gitHubApi.getIssues(owner, repo)

            if (response.isSuccessful) {
                val issues = response.body() ?: emptyList()
                val tasks = GitHubAdapter.toTaskList(issues, projectId)

                tasks.forEach { task ->
                    taskDao.insert(task)
                }

                Result.success(tasks.size)
            } else {
                Result.failure(Exception("Erro HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro de conexão: ${e.message}"))
        }
    }

    suspend fun testGitHubConnection(owner: String = "google", repo: String = "material-design-icons"): Boolean {
        return try {
            val response = gitHubApi.getIssues(owner, repo)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}