package kz.studyplanner.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionResponse(
    val items: List<QuestionDto>? = null,

    @SerialName("has_more")
    val hasMore: Boolean = false,

    @SerialName("quota_remaining")
    val quotaRemaining: Int? = null,

    val backoff: Int? = null,

    @SerialName("error_id")
    val errorId: Int? = null,

    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
data class QuestionDto(
    @SerialName("question_id")
    val questionId: Long = 0,

    val title: String? = null,

    val body: String? = null,

    val link: String? = null,

    val tags: List<String>? = null,

    val score: Int = 0,

    @SerialName("is_answered")
    val isAnswered: Boolean = false,

    val owner: QuestionOwnerDto? = null
)

@Serializable
data class QuestionOwnerDto(
    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("profile_image")
    val profileImage: String? = null
)