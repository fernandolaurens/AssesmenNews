package com.laurens.assesmennews.data.remote.retrofit

import com.laurens.assesmennews.data.remote.response.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("articles/")
    suspend fun getNews(
        @Query("title_contains") title: String?,
        @Query("news_site") newsSite: String?,
        @Query("_limit") limit: Int = 10
    ): Response<NewsResponse>

    @GET("news_sites/")
    suspend fun getNewsSites(): Response<List<String>>
}