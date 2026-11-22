package kz.studyplanner.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException
import kotlinx.coroutines.runBlocking
import kz.studyplanner.data.remote.QuestionDto
import kz.studyplanner.data.remote.QuestionResponse
import kz.studyplanner.data.remote.StudyLabApi
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyLabRepositoryTest {

    @Test
    fun sendsSelectedTopicTagToApi() = runBlocking {
        val api = FakeStudyLabApi()
        val repository = StudyLabRepository(api)

        for (topic in StudyTopic.entries) {
            repository.getQuestions(topic)

            assertEquals(topic.tag, api.lastTag)
        }
    }

    @Test
    fun keepsValidQuestionsAndRemovesIncompleteOnes() = runBlocking {
        val api = FakeStudyLabApi()

        val valid = QuestionDto(
            questionId = 1,
            title = "Kotlin question",
            link = "https://stackoverflow.com/questions/1"
        )

        api.response = QuestionResponse(
            items = listOf(
                valid,
                valid.copy(
                    questionId = 0
                ),
                valid.copy(
                    questionId = 2,
                    title = " "
                ),
                valid.copy(
                    questionId = 3,
                    link = null
                )
            )
        )

        val result = StudyLabRepository(api).getQuestions(
            StudyTopic.MOBILE
        )

        assertEquals(listOf(valid), result)
    }

    @Test
    fun emptyItemsReturnEmptyList() = runBlocking {
        val repository = StudyLabRepository(
            FakeStudyLabApi()
        )

        val result = repository.getQuestions(
            StudyTopic.MOBILE
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun apiErrorIsNotTreatedAsEmptyResult() = runBlocking {
        val api = FakeStudyLabApi()

        api.response = QuestionResponse(
            errorId = 400,
            errorMessage = "Invalid request"
        )

        try {
            StudyLabRepository(api).getQuestions(
                StudyTopic.MOBILE
            )

            fail("API error must be reported")
        } catch (expected: IllegalStateException) {
            assertEquals(
                "Invalid request",
                expected.message
            )
        }
    }

    @Test
    fun missingItemsAreRejected() = runBlocking {
        val api = FakeStudyLabApi()

        api.response = QuestionResponse()

        try {
            StudyLabRepository(api).getQuestions(
                StudyTopic.MOBILE
            )

            fail("Missing items must be rejected")
        } catch (expected: IllegalStateException) {
            assertEquals(
                "The service returned an invalid response.",
                expected.message
            )
        }
    }

    @Test
    fun requestCanBeRetriedAfterConnectionFailure() = runBlocking {
        val api = FakeStudyLabApi()
        val repository = StudyLabRepository(api)

        api.failure = IOException("No connection")

        try {
            repository.getQuestions(
                StudyTopic.DEVOPS
            )

            fail("Connection error must be propagated")
        } catch (expected: IOException) {
            assertEquals(
                "No connection",
                expected.message
            )
        }

        val question = QuestionDto(
            questionId = 10,
            title = "Docker question",
            link = "https://stackoverflow.com/questions/10"
        )

        api.failure = null

        api.response = QuestionResponse(
            items = listOf(question)
        )

        val result = repository.getQuestions(
            StudyTopic.DEVOPS
        )

        assertEquals(listOf(question), result)
    }

    private class FakeStudyLabApi : StudyLabApi {

        var response = QuestionResponse(
            items = emptyList()
        )

        var failure: IOException? = null

        var lastTag: String? = null

        override suspend fun getQuestions(
            tag: String,
            site: String,
            sort: String,
            order: String,
            pageSize: Int,
            filter: String
        ): QuestionResponse {
            lastTag = tag

            failure?.let {
                throw it
            }

            return response
        }
    }
}