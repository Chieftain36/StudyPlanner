package kz.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kz.studyplanner.data.StudyLabRepository
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

class StudyLabViewModel : ViewModel() {

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

    init {
        loadQuestions()
    }

    fun selectTopic(topic: StudyTopic) {
        if (_selectedTopic.value == topic) {
            return
        }

        _selectedTopic.value = topic
        loadQuestions()
    }

    fun retry() {
        loadQuestions()
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
}