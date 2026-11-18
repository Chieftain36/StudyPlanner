package kz.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kz.studyplanner.data.StudyTask
import kz.studyplanner.data.TaskRepository
import java.time.LocalDate

class PlannerViewModel : ViewModel() {

    private val repository = TaskRepository()

    val tasks: StateFlow<List<StudyTask>> = repository.tasks

    fun validateTask(
        title: String,
        course: String,
        dueDate: String
    ): String? {
        if (title.isBlank()) {
            return "Enter a task title."
        }

        if (course.isBlank()) {
            return "Enter a course name."
        }

        val date = dueDate.trim()

        val correctFormat =
            Regex("""\d{4}-\d{2}-\d{2}""").matches(date)

        val validDate = runCatching {
            LocalDate.parse(date)
        }.isSuccess

        if (!correctFormat || !validDate) {
            return "Enter a valid date in YYYY-MM-DD format."
        }

        return null
    }

    fun addTask(
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Long {
        require(validateTask(title, course, dueDate) == null) {
            "Invalid task data"
        }

        return repository.addTask(
            title = title,
            course = course,
            dueDate = dueDate.trim(),
            notes = notes
        )
    }

    fun toggleTaskCompletion(taskId: Long) {
        repository.toggleTaskCompletion(taskId)
    }

    fun deleteTask(taskId: Long) {
        repository.deleteTask(taskId)
    }
}