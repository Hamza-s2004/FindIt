package com.example.findit.data.model

/**
 * Parent table for items. Linked via FK from `items.category_id`.
 * F2 → DatabaseHelper seeds these on onCreate().
 */
data class Category(
    val id: Long,
    val name: String,
    val emoji: String = ""
) {
    val display: String get() = if (emoji.isBlank()) name else "$emoji $name"
}
