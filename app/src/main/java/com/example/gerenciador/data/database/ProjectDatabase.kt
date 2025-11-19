package com.example.gerenciador.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.gerenciador.data.dao.ProjectDao
import com.example.gerenciador.data.dao.TaskDao
import com.example.gerenciador.data.model.Project
import com.example.gerenciador.data.model.Task

@Database(
    entities = [Project::class, Task::class],
    version = 2,
    exportSchema = false
)
abstract class ProjectDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: ProjectDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adiciona as novas colunas de timer
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN tempoTrabalhado INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN timerAtivo INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN ultimoInicioTimer INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): ProjectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProjectDatabase::class.java,
                    "project_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}