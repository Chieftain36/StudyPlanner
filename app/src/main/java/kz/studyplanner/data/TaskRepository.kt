package kz.studyplanner.data

import kotlinx.coroutines.flow.Flow
import kz.studyplanner.data.local.TaskDao

class TaskRepository(
    private val dao: TaskDao
) {
    val tasks: Flow<List<StudyTask>> = dao.observeTasks()

    suspend fun addTask(
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Long {
        require(title.isNotBlank()) {
            "Title cannot be blank"
        }

        require(course.isNotBlank()) {
            "Course cannot be blank"
        }

        return dao.insertTask(
            StudyTask(
                title = title.trim(),
                course = course.trim(),
                dueDate = dueDate.trim(),
                notes = notes.trim()
            )
        )
    }

    suspend fun updateTask(
        taskId: Long,
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Boolean {
        require(title.isNotBlank()) {
            "Title cannot be blank"
        }

        require(course.isNotBlank()) {
            "Course cannot be blank"
        }

        return dao.updateTask(
            taskId = taskId,
            title = title.trim(),
            course = course.trim(),
            dueDate = dueDate.trim(),
            notes = notes.trim()
        ) > 0
    }

    suspend fun toggleTaskCompletion(taskId: Long) {
        dao.toggleTaskCompletion(taskId)
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTask(taskId)
    }
}