package kz.studyplanner.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kz.studyplanner.data.StudyLabRepository
import kz.studyplanner.data.StudyPreferencesRepository
import kz.studyplanner.data.StudyTopic
import kz.studyplanner.data.remote.QuestionDto
import retrofit2.HttpException

sealed interface StudyLabUiState {

    data object Loading : StudyLabUiState

    data class Success(
        val questions: List<QuestionDto>
    ) : StudyLabUiState

    data object Empty : StudyLabUiState

    data class Error(
        val message: String
    ) : StudyLabUiState
}

class StudyLabViewModel(
    private val preferencesRepository: StudyPreferencesRepository
) : ViewModel() {

    private val repository = StudyLabRepository()

    private val _selectedTopic =
        MutableStateFlow(StudyTopic.MOBILE)

    val selectedTopic = _selectedTopic.asStateFlow()

    private val _uiState =
        MutableStateFlow<StudyLabUiState>(
            StudyLabUiState.Loading
        )

    val uiState = _uiState.asStateFlow()

    private var loadingJob: Job? = null
    private var preferencesJob: Job? = null

    private var preferencesLoaded = false

    init {
        observePreferences()
    }

    private fun observePreferences() {
        preferencesJob?.cancel()

        _uiState.value = StudyLabUiState.Loading

        preferencesJob = viewModelScope.launch {
            try {
                preferencesRepository.selectedTopic.collect { topic ->
                    val shouldLoad =
                        !preferencesLoaded ||
                                _selectedTopic.value != topic

                    preferencesLoaded = true
                    _selectedTopic.value = topic

                    if (shouldLoad) {
                        loadQuestions()
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                preferencesLoaded = false
                loadingJob?.cancel()

                _uiState.value = StudyLabUiState.Error(
                    "Could not read your saved topic. Tap Retry."
                )
            }
        }
    }

    fun selectTopic(topic: StudyTopic) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveTopic(topic)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                loadingJob?.cancel()

                _uiState.value = StudyLabUiState.Error(
                    "Could not save your topic. Please select it again."
                )
            }
        }
    }

    fun retry() {
        if (preferencesLoaded) {
            loadQuestions()
        } else {
            observePreferences()
        }
    }

    private fun loadQuestions() {
        loadingJob?.cancel()

        val topic = _selectedTopic.value

        loadingJob = viewModelScope.launch {
            _uiState.value = StudyLabUiState.Loading

            try {
                val questions = repository.getQuestions(topic)

                _uiState.value = if (questions.isEmpty()) {
                    StudyLabUiState.Empty
                } else {
                    StudyLabUiState.Success(questions)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                _uiState.value = StudyLabUiState.Error(
                    "Could not connect. Check your internet and try again."
                )
            } catch (exception: HttpException) {
                _uiState.value = StudyLabUiState.Error(
                    "Server error (${exception.code()}). Try again later."
                )
            } catch (exception: SerializationException) {
                _uiState.value = StudyLabUiState.Error(
                    "Could not read the server response."
                )
            } catch (exception: Exception) {
                _uiState.value = StudyLabUiState.Error(
                    exception.message
                        ?: "Could not load questions. Try again."
                )
            }
        }
    }

    companion object {

        fun factory(
            context: Context
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StudyLabViewModel(
                    StudyPreferencesRepository(
                        context.applicationContext
                    )
                )
            }
        }
    }
}