package kz.studyplanner.data

import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kz.studyplanner.data.remote.ApiClient
import kz.studyplanner.data.remote.QuestionDto
import kz.studyplanner.data.remote.StudyLabApi

class StudyLabRepository(
    private val api: StudyLabApi = ApiClient.studyLabApi
) {
    private val requestMutex = Mutex()
    private var nextRequestTime = 0L

    suspend fun getQuestions(
        topic: StudyTopic
    ): List<QuestionDto> = requestMutex.withLock {
        val waitTime =
            nextRequestTime - SystemClock.elapsedRealtime()

        if (waitTime > 0) {
            delay(waitTime)
        }

        val response = api.getQuestions(tag = topic.tag)

        val backoffSeconds = response.backoff
            ?.coerceAtLeast(0)
            ?: 0

        nextRequestTime = SystemClock.elapsedRealtime() +
                backoffSeconds.toLong() * 1000L

        if (response.errorId != null) {
            throw IllegalStateException(
                response.errorMessage
                    ?: "The service could not load questions."
            )
        }

        val questions = response.items
            ?: throw IllegalStateException(
                "The service returned an invalid response."
            )

        questions.filter { question ->
            question.questionId > 0 &&
                    !question.title.isNullOrBlank() &&
                    !question.link.isNullOrBlank()
        }
    }
}