package kz.studyplanner.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TaskRepository {

    private var nextId = 3L

    private val _tasks = MutableStateFlow(
        listOf(
            StudyTask(
                id = 1L,
                title = "Prepare project architecture",
                course = "CSS 216",
                dueDate = "2026-11-25",
                notes = "Explain Model, View and ViewModel."
            ),
            StudyTask(
                id = 2L,
                title = "Review Kotlin collections",
                course = "CSS 216",
                dueDate = "2026-11-26",
                isCompleted = true
            )
        )
    )

    val tasks: StateFlow<List<StudyTask>> = _tasks.asStateFlow()

    fun addTask(
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Long {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(course.isNotBlank()) { "Course cannot be blank" }

        val task = StudyTask(
            id = nextId++,
            title = title.trim(),
            course = course.trim(),
            dueDate = dueDate,
            notes = notes.trim()
        )

        _tasks.update { currentTasks ->
            currentTasks + task
        }

        return task.id
    }

    fun toggleTaskCompletion(taskId: Long) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(isCompleted = !task.isCompleted)
                } else {
                    task
                }
            }
        }
    }

    fun deleteTask(taskId: Long) {
        _tasks.update { currentTasks ->
            currentTasks.filterNot { task ->
                task.id == taskId
            }
        }
    }
}