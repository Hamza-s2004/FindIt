package com.example.findit.data.model

import com.google.gson.annotations.SerializedName

/**
 * F1 — Raw JSON shape from JSONPlaceholder /posts.
 * Mapped into [Item] by the repository before being shown / cached.
 */
data class ApiPost(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
