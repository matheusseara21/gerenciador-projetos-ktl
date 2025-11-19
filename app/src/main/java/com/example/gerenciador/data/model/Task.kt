package com.example.gerenciador.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val titulo: String,
    val descricao: String,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataConclusao: Long? = null,
    val status: TaskStatus = TaskStatus.PENDENTE,
    // Campos para timer
    val tempoTrabalhado: Long = 0L, // Tempo em milissegundos
    val timerAtivo: Boolean = false, // Se o timer está rodando
    val ultimoInicioTimer: Long = 0L // Timestamp de quando foi iniciado
)

enum class TaskStatus {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA
}