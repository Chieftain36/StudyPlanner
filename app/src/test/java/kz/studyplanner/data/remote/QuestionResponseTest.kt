package kz.studyplanner.data.remote

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class QuestionResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun decodesQuestionAndAuthorFromApiFieldNames() {
        val response = json.decodeFromString<QuestionResponse>(
            """
            {
              "items": [{
                "question_id": 123,
                "title": "How does Kotlin work?",
                "body": "<p>Example question</p>",
                "link": "https://stackoverflow.com/questions/123",
                "tags": ["kotlin"],
                "score": 7,
                "is_answered": true,
                "owner": {
                  "display_name": "Student",
                  "profile_image": "https://example.com/avatar.png",
                  "unknown_owner_field": true
                },
                "unknown_question_field": 42
              }],
              "has_more": true,
              "quota_remaining": 250,
              "unknown_response_field": "ignored"
            }
            """.trimIndent()
        )

        val question = response.items!!.single()

        assertEquals(123L, question.questionId)
        assertEquals("How does Kotlin work?", question.title)
        assertEquals("<p>Example question</p>", question.body)

        assertEquals(
            "https://stackoverflow.com/questions/123",
            question.link
        )

        assertEquals(listOf("kotlin"), question.tags)
        assertEquals(7, question.score)
        assertTrue(question.isAnswered)

        assertEquals(
            "Student",
            question.owner?.displayName
        )

        assertEquals(
            "https://example.com/avatar.png",
            question.owner?.profileImage
        )

        assertTrue(response.hasMore)
        assertEquals(250, response.quotaRemaining)
    }

    @Test
    fun decodesEmptyResultWithoutTreatingItAsMissingItems() {
        val response = json.decodeFromString<QuestionResponse>(
            """{"items": [], "has_more": false}"""
        )

        assertNotNull(response.items)
        assertTrue(response.items!!.isEmpty())
        assertNull(response.errorId)
    }

    @Test
    fun decodesErrorResponseWithoutItems() {
        val response = json.decodeFromString<QuestionResponse>(
            """
            {
              "error_id": 502,
              "error_message": "Too many requests",
              "backoff": 10
            }
            """.trimIndent()
        )

        assertNull(response.items)
        assertEquals(502, response.errorId)
        assertEquals("Too many requests", response.errorMessage)
        assertEquals(10, response.backoff)
    }
}