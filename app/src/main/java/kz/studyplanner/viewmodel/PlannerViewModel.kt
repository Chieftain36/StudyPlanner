package kz.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kz.studyplanner.data.StudyTask
import kz.studyplanner.data.TaskRepository

class PlannerViewModel : ViewModel() {

    private val repository = TaskRepository()

    val tasks: StateFlow<List<StudyTask>> = repository.tasks

    fun addTask(
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Long {
        return repository.addTask(
            title = title,
            course = course,
            dueDate = dueDate,
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