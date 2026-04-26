package com.example.findit.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.findit.data.api.ApiService
import com.example.findit.data.api.RetrofitClient
import com.example.findit.data.db.DatabaseHelper
import com.example.findit.data.db.DatabaseHelper.Companion.CAT_EMOJI
import com.example.findit.data.db.DatabaseHelper.Companion.CAT_ID
import com.example.findit.data.db.DatabaseHelper.Companion.CAT_NAME
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_CATEGORY_ID
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_CONTACT
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_CREATED_AT
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_DATE
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_DESC
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_ID
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_LOCATION
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_REMOTE_ID
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_SOURCE
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_TITLE
import com.example.findit.data.db.DatabaseHelper.Companion.ITEM_TYPE
import com.example.findit.data.db.DatabaseHelper.Companion.TBL_CATEGORIES
import com.example.findit.data.db.DatabaseHelper.Companion.TBL_ITEMS
import com.example.findit.data.model.ApiPost
import com.example.findit.data.model.Category
import com.example.findit.data.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Single source of truth for the UI layer.
 *
 * Logic-map:
 *   F1 → fetchApiData()           — Retrofit call
 *   F2 → handled in DatabaseHelper (schema)
 *   F3 → insertItem / getAllItems / updateItem / deleteItem
 *   F4 → syncApiToDb()            — caches API rows into SQLite
 *   F5 → searchItems()            — LIKE + ORDER BY + filters
 *
 * EVERY public function suspends and switches to Dispatchers.IO so callers
 * never block the main thread, regardless of which fragment invokes it.
 */
class ItemRepository(
    context: Context,
    private val api: ApiService = RetrofitClient.apiService
) {

    private val dbHelper = DatabaseHelper(context.applicationContext)

    // -------------------------------------------------------------------------
    // F1 — REST API
    // -------------------------------------------------------------------------

    /** Raw API call — returns parsed JSON. */
    suspend fun fetchApiData(limit: Int = 20): List<ApiPost> = withContext(Dispatchers.IO) {
        api.getPosts(limit)
    }

    // -------------------------------------------------------------------------
    // F4 — API → SQLite cache (Option A: offline-first)
    // -------------------------------------------------------------------------

    /**
     * Pulls the latest /posts from the network and upserts each into the items
     * table tagged with source = "API". Returns the number of rows touched.
     *
     * If the API fails the local DB is left untouched and the exception bubbles
     * up so the caller can show a Toast/Snackbar.
     */
    suspend fun syncApiToDb(limit: Int = 20): Int = withContext(Dispatchers.IO) {
        val posts = api.getPosts(limit)
        val cats = getCategoriesInternal()
        if (cats.isEmpty()) return@withContext 0

        val db = dbHelper.writableDatabase
        var touched = 0
        db.beginTransaction()
        try {
            posts.forEach { post ->
                // Spread API rows across categories deterministically so they
                // exercise the FK relationship.
                val category = cats[post.id % cats.size]

                val cv = ContentValues().apply {
                    put(ITEM_TITLE, post.title.take(80))
                    put(ITEM_DESC, post.body)
                    put(ITEM_TYPE, Item.TYPE_FOUND) // API posts surface as "Found"
                    put(ITEM_CATEGORY_ID, category.id)
                    put(ITEM_LOCATION, "Reported online")
                    put(ITEM_DATE, "")
                    put(ITEM_CONTACT, "user-${post.userId}@findit.app")
                    put(ITEM_SOURCE, Item.SOURCE_API)
                    put(ITEM_REMOTE_ID, post.id)
                    put(ITEM_CREATED_AT, System.currentTimeMillis())
                }

                // Upsert by remote_id so repeated syncs don't duplicate.
                val updated = db.update(
                    TBL_ITEMS, cv,
                    "$ITEM_REMOTE_ID = ?",
                    arrayOf(post.id.toString())
                )
                if (updated == 0) {
                    db.insert(TBL_ITEMS, null, cv)
                }
                touched++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        touched
    }

    // -------------------------------------------------------------------------
    // F3 — CRUD
    // -------------------------------------------------------------------------

    /** Create */
    suspend fun insertItem(item: Item): Long = withContext(Dispatchers.IO) {
        val cv = item.toContentValues()
        dbHelper.writableDatabase.insert(TBL_ITEMS, null, cv)
    }

    /** Read — all items, newest first */
    suspend fun getAllItems(): List<Item> = withContext(Dispatchers.IO) {
        queryItems(orderBy = orderClause(Item.SORT_NEWEST))
    }

    /** Read — single item by primary key */
    suspend fun getItemById(id: Long): Item? = withContext(Dispatchers.IO) {
        queryItems(
            where = "i.$ITEM_ID = ?",
            args = arrayOf(id.toString())
        ).firstOrNull()
    }

    /** Read — items matching a type ("Lost" / "Found" / "All") */
    suspend fun getItemsByType(type: String): List<Item> = withContext(Dispatchers.IO) {
        if (type == Item.TYPE_ALL) return@withContext getAllItems()
        queryItems(
            where = "i.$ITEM_TYPE = ?",
            args = arrayOf(type),
            orderBy = orderClause(Item.SORT_NEWEST)
        )
    }

    /** Update */
    suspend fun updateItem(item: Item): Int = withContext(Dispatchers.IO) {
        if (item.id <= 0L) return@withContext 0
        dbHelper.writableDatabase.update(
            TBL_ITEMS,
            item.toContentValues(),
            "$ITEM_ID = ?",
            arrayOf(item.id.toString())
        )
    }

    /** Delete */
    suspend fun deleteItem(id: Long): Int = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete(
            TBL_ITEMS, "$ITEM_ID = ?", arrayOf(id.toString())
        )
    }

    // -------------------------------------------------------------------------
    // F5 — Dynamic SQL: search (LIKE) + sort (ORDER BY) + optional filters
    // -------------------------------------------------------------------------

    suspend fun searchItems(
        query: String = "",
        type: String = Item.TYPE_ALL,
        categoryId: Long? = null,
        fromDate: String? = null,
        toDate: String? = null,
        sort: String = Item.SORT_NEWEST
    ): List<Item> = withContext(Dispatchers.IO) {
        val where = StringBuilder("1=1")
        val args = mutableListOf<String>()

        if (query.isNotBlank()) {
            // Search across title, description, location AND category name.
            where.append(" AND (i.$ITEM_TITLE LIKE ?")
            where.append(" OR i.$ITEM_DESC LIKE ?")
            where.append(" OR i.$ITEM_LOCATION LIKE ?")
            where.append(" OR c.$CAT_NAME LIKE ?)")
            val pattern = "%${query.trim()}%"
            repeat(4) { args.add(pattern) }
        }
        if (type != Item.TYPE_ALL) {
            where.append(" AND i.$ITEM_TYPE = ?")
            args.add(type)
        }
        categoryId?.let {
            where.append(" AND i.$ITEM_CATEGORY_ID = ?")
            args.add(it.toString())
        }
        if (!fromDate.isNullOrBlank()) {
            where.append(" AND i.$ITEM_DATE >= ?")
            args.add(fromDate)
        }
        if (!toDate.isNullOrBlank()) {
            where.append(" AND i.$ITEM_DATE <= ?")
            args.add(toDate)
        }

        queryItems(
            where = where.toString(),
            args = args.toTypedArray(),
            orderBy = orderClause(sort)
        )
    }

    // -------------------------------------------------------------------------
    // Categories
    // -------------------------------------------------------------------------

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        getCategoriesInternal()
    }

    suspend fun getCategoryByName(name: String): Category? = withContext(Dispatchers.IO) {
        getCategoriesInternal().firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private fun getCategoriesInternal(): List<Category> {
        val list = mutableListOf<Category>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT $CAT_ID, $CAT_NAME, $CAT_EMOJI FROM $TBL_CATEGORIES ORDER BY $CAT_NAME ASC;",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Category(
                        id = c.getLong(0),
                        name = c.getString(1),
                        emoji = c.getString(2) ?: ""
                    )
                )
            }
        }
        return list
    }

    /** Centralised SELECT joining items + categories so we always get the name. */
    private fun queryItems(
        where: String = "1=1",
        args: Array<String> = emptyArray(),
        orderBy: String = orderClause(Item.SORT_NEWEST),
        limit: Int? = null
    ): List<Item> {
        val sql = buildString {
            append("SELECT i.$ITEM_ID, i.$ITEM_TITLE, i.$ITEM_DESC, i.$ITEM_TYPE, ")
            append("i.$ITEM_CATEGORY_ID, c.$CAT_NAME, ")
            append("i.$ITEM_LOCATION, i.$ITEM_DATE, i.$ITEM_CONTACT, ")
            append("i.$ITEM_SOURCE, i.$ITEM_REMOTE_ID, i.$ITEM_CREATED_AT ")
            append("FROM $TBL_ITEMS i ")
            append("INNER JOIN $TBL_CATEGORIES c ON c.$CAT_ID = i.$ITEM_CATEGORY_ID ")
            append("WHERE ").append(where).append(' ')
            append("ORDER BY ").append(orderBy)
            if (limit != null) append(" LIMIT ").append(limit)
            append(';')
        }
        val list = mutableListOf<Item>()
        dbHelper.readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) list.add(c.toItem())
        }
        return list
    }

    private fun orderClause(sort: String): String = when (sort) {
        Item.SORT_OLDEST -> "i.$ITEM_CREATED_AT ASC"
        Item.SORT_TITLE_ASC -> "i.$ITEM_TITLE COLLATE NOCASE ASC"
        Item.SORT_TITLE_DESC -> "i.$ITEM_TITLE COLLATE NOCASE DESC"
        else -> "i.$ITEM_CREATED_AT DESC"
    }

    private fun Item.toContentValues(): ContentValues = ContentValues().apply {
        put(ITEM_TITLE, title)
        put(ITEM_DESC, description)
        put(ITEM_TYPE, type)
        put(ITEM_CATEGORY_ID, categoryId)
        put(ITEM_LOCATION, location)
        put(ITEM_DATE, date)
        put(ITEM_CONTACT, contact)
        put(ITEM_SOURCE, source)
        if (remoteId != null) put(ITEM_REMOTE_ID, remoteId) else putNull(ITEM_REMOTE_ID)
        put(ITEM_CREATED_AT, createdAt)
    }

    private fun Cursor.toItem(): Item = Item(
        id = getLong(0),
        title = getString(1) ?: "",
        description = getString(2) ?: "",
        type = getString(3) ?: Item.TYPE_LOST,
        categoryId = getLong(4),
        categoryName = getString(5) ?: "",
        location = getString(6) ?: "",
        date = getString(7) ?: "",
        contact = getString(8) ?: "",
        source = getString(9) ?: Item.SOURCE_LOCAL,
        remoteId = if (isNull(10)) null else getInt(10),
        createdAt = getLong(11)
    )
}
