package com.example.findit.data.model

/**
 * Domain model representing a lost or found item.
 *
 * Used by:
 *  - F2 SQLiteOpenHelper (rows in `items` table, FK -> categories.id)
 *  - F1 API mapper (ApiPost -> Item)
 *  - F3 CRUD UI (PostFragment / EditItemFragment / DetailFragment)
 *  - F5 Search/Sort results
 */
data class Item(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val type: String = TYPE_LOST,
    val categoryId: Long,
    val categoryName: String = "",
    val location: String = "",
    val date: String = "",
    val contact: String = "",
    val source: String = SOURCE_LOCAL,
    val remoteId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_LOST = "Lost"
        const val TYPE_FOUND = "Found"
        const val TYPE_ALL = "All"

        const val SOURCE_LOCAL = "LOCAL"
        const val SOURCE_API = "API"

        const val SORT_NEWEST = "newest"
        const val SORT_OLDEST = "oldest"
        const val SORT_TITLE_ASC = "title_asc"
        const val SORT_TITLE_DESC = "title_desc"
    }
}
