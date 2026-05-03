package com.example.findit.data.model

import com.google.gson.annotations.SerializedName
data class ApiPost(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
