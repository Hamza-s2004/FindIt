package com.example.findit.data.api

import com.example.findit.data.model.ApiPost
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * F1 — Retrofit interface backing the public REST API.
 *
 * We use jsonplaceholder.typicode.com (free, no key) and treat each /posts entry
 * as a "Found" item posting; it gives us realistic JSON to parse and cache.
 */
interface ApiService {

    @GET("posts")
    suspend fun getPosts(@Query("_limit") limit: Int = 20): List<ApiPost>
}
