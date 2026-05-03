package com.example.findit.data.api

import com.example.findit.data.model.ApiPost
import retrofit2.http.GET
import retrofit2.http.Query
interface ApiService {

    @GET("posts")
    suspend fun getPosts(@Query("_limit") limit: Int = 20): List<ApiPost>
}
