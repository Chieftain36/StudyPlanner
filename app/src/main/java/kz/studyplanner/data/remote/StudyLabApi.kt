package kz.studyplanner.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface StudyLabApi {

    @GET("questions")
    suspend fun getQuestions(
        @Query("tagged")
        tag: String,

        @Query("site")
        site: String = "stackoverflow",

        @Query("sort")
        sort: String = "votes",

        @Query("order")
        order: String = "desc",

        @Query("pagesize")
        pageSize: Int = 20,

        @Query("filter")
        filter: String = "withbody"
    ): QuestionResponse
}