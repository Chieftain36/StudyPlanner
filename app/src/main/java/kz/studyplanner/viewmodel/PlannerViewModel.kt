package kz.studyplanner.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.studyplanner.data.StudyTask
import kz.studyplanner.data.TaskRepository
import kz.studyplanner.data.local.PlannerDatabase

class PlannerViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _tasks =
        MutableStateFlow<List<StudyTask>>(emptyList())

    val tasks = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    val isLoading = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)

    val loadError = _loadError.asStateFlow()

    private var loadingJob: Job? = null

    init {
        retryLoading()
    }

    fun retryLoading() {
        loadingJob?.cancel()

        loadingJob = viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null

            try {
                repository.tasks.collect { savedTasks ->
                    _tasks.value = savedTasks
                    _isLoading.value = false
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _isLoading.value = false
                _loadError.value =
                    "Could not read saved tasks. Please retry."
            }
        }
    }

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

    suspend fun addTask(
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Long {
        require(validateTask(title, course, dueDate) == null) {
            "Invalid task data"
        }

        return repository.addTask(
            title,
            course,
            dueDate,
            notes
        )
    }

    suspend fun updateTask(
        taskId: Long,
        title: String,
        course: String,
        dueDate: String,
        notes: String
    ): Boolean {
        require(validateTask(title, course, dueDate) == null) {
            "Invalid task data"
        }

        return repository.updateTask(
            taskId,
            title,
            course,
            dueDate,
            notes
        )
    }

    suspend fun toggleTaskCompletion(taskId: Long) {
        repository.toggleTaskCompletion(taskId)
    }

    suspend fun deleteTask(taskId: Long) {
        repository.deleteTask(taskId)
    }

    companion object {

        fun factory(
            context: Context
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlannerViewModel(
                    TaskRepository(
                        PlannerDatabase.getDatabase(
                            context.applicationContext
                        ).taskDao()
                    )
                )
            }
        }
    }
}