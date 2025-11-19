package com.example.gerenciador.data.dao

import androidx.room.*
import com.example.gerenciador.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Inserir uma nova tarefa
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    // Atualizar uma tarefa existente
    @Update
    suspend fun update(task: Task): Int

    // Deletar uma tarefa
    @Delete
    suspend fun delete(task: Task): Int

    // Buscar tarefa por ID
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    // Buscar todas as tarefas de um projeto
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY dataCriacao DESC")
    fun getByProject(projectId: Long): Flow<List<Task>>

    // Buscar tarefas por status
    @Query("SELECT * FROM tasks WHERE status = :status AND projectId = :projectId ORDER BY dataCriacao DESC")
    fun getByStatus(projectId: Long, status: String): Flow<List<Task>>

    // Buscar tarefas pendentes (para dashboard)
    @Query("SELECT * FROM tasks WHERE status != 'CONCLUIDA' ORDER BY dataCriacao DESC")
    fun getPendingTasks(): Flow<List<Task>>

    // Contar tarefas por projeto
    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId")
    fun getCountByProject(projectId: Long): Flow<Int>

    // Contar tarefas por status
    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND status = :status")
    fun getCountByStatus(projectId: Long, status: String): Flow<Int>

    // Deletar todas tarefas de um projeto (cascade)
    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long): Int

    // Atualizar o status da tarefa
    @Query("UPDATE tasks SET status = :newStatus WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, newStatus: String)
}