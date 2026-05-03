package com.example.findit.data.model

data class Category(
    val id: Long,
    val name: String,
    val emoji: String = ""
) {
    val display: String get() = if (emoji.isBlank()) name else "$emoji $name"
}
