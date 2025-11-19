package com.example.gerenciador.presentation.navigation

sealed class Screen(val route: String) {
    object ProjectList : Screen("project_list")
    object AddProject : Screen("add_project?projectId={projectId}") {
        fun withId(id: Long): String {
            return "add_project?projectId=$id"
        }
        fun create(): String {
            return "add_project"
        }
    }

    object ProjectDetails : Screen("project_details/{projectId}") {
        fun withId(id: Long): String {
            return "project_details/$id"
        }
    }

    object TaskEdit : Screen("project_details/{projectId}/task_edit?taskId={taskId}") {
        fun create(projectId: Long): String {
            return "project_details/$projectId/task_edit?taskId=-1"
        }
        fun withId(projectId: Long, taskId: Long): String {
            return "project_details/$projectId/task_edit?taskId=$taskId"
        }
    }

    // Rota de import do github
    object ImportRepository : Screen("import_repository")
}