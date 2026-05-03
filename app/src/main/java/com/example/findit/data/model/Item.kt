package com.example.findit.data.model

data class Item(
    var id: Long = 0L,
    var title: String = "",
    var description: String = "",
    var type: String = TYPE_LOST,
    var categoryId: Long = 0L,
    var categoryName: String = "",
    var location: String = "",
    var date: String = "",
    var contact: String = "",
    var source: String = SOURCE_LOCAL,

    // 🔥 FIX: MUST be String (Firestore doc ID)
    var remoteId: String? = null,

    var createdAt: Long = System.currentTimeMillis()
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