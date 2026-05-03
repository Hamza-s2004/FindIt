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

class ItemRepository(
    context: Context,
    private val api: ApiService = RetrofitClient.apiService
) {

    private val dbHelper = DatabaseHelper(context.applicationContext)

    // ---------------- API ----------------

    suspend fun fetchApiData(limit: Int = 20): List<ApiPost> = withContext(Dispatchers.IO) {
        api.getPosts(limit)
    }

    // ---------------- CRUD ----------------

    suspend fun insertItem(item: Item): Long = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.insert(TBL_ITEMS, null, item.toContentValues())
    }

    suspend fun getAllItems(): List<Item> = withContext(Dispatchers.IO) {
        queryItems(
            where = "1=1",
            args = emptyArray(),
            orderBy = orderClause(Item.SORT_NEWEST)
        )
    }

    suspend fun getItemById(id: Long): Item? = withContext(Dispatchers.IO) {
        queryItems(
            where = "i.$ITEM_ID = ?",
            args = arrayOf(id.toString()),
            orderBy = orderClause(Item.SORT_NEWEST)
        ).firstOrNull()
    }

    suspend fun updateItem(item: Item): Int = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.update(
            TBL_ITEMS,
            item.toContentValues(),
            "$ITEM_ID = ?",
            arrayOf(item.id.toString())
        )
    }

    suspend fun deleteItem(id: Long): Int = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete(
            TBL_ITEMS,
            "$ITEM_ID = ?",
            arrayOf(id.toString())
        )
    }

    // ---------------- SEARCH ----------------

    suspend fun searchItems(
        query: String = "",
        type: String = Item.TYPE_ALL,
        categoryId: Long? = null,
        sort: String = Item.SORT_NEWEST
    ): List<Item> = withContext(Dispatchers.IO) {

        val where = StringBuilder("1=1")
        val args = mutableListOf<String>()

        if (query.isNotBlank()) {
            where.append(" AND (i.$ITEM_TITLE LIKE ? OR i.$ITEM_DESC LIKE ?)")
            val q = "%$query%"
            args.add(q)
            args.add(q)
        }

        if (type != Item.TYPE_ALL) {
            where.append(" AND i.$ITEM_TYPE = ?")
            args.add(type)
        }

        categoryId?.let {
            where.append(" AND i.$ITEM_CATEGORY_ID = ?")
            args.add(it.toString())
        }

        queryItems(
            where = where.toString(),
            args = args.toTypedArray(),
            orderBy = orderClause(sort)
        )
    }

    // ---------------- INTERNAL ----------------

    private fun queryItems(
        where: String,
        args: Array<String>,
        orderBy: String
    ): List<Item> {

        val sql = """
            SELECT i.$ITEM_ID, i.$ITEM_TITLE, i.$ITEM_DESC, i.$ITEM_TYPE,
                   i.$ITEM_CATEGORY_ID, c.$CAT_NAME,
                   i.$ITEM_LOCATION, i.$ITEM_DATE, i.$ITEM_CONTACT,
                   i.$ITEM_SOURCE, i.$ITEM_REMOTE_ID, i.$ITEM_CREATED_AT
            FROM $TBL_ITEMS i
            INNER JOIN $TBL_CATEGORIES c ON c.$CAT_ID = i.$ITEM_CATEGORY_ID
            WHERE $where
            ORDER BY $orderBy
        """.trimIndent()

        val list = mutableListOf<Item>()

        dbHelper.readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                list.add(c.toItem())
            }
        }

        return list
    }

    private fun orderClause(sort: String): String = when (sort) {
        Item.SORT_OLDEST -> "i.$ITEM_CREATED_AT ASC"
        Item.SORT_TITLE_ASC -> "i.$ITEM_TITLE ASC"
        Item.SORT_TITLE_DESC -> "i.$ITEM_TITLE DESC"
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
        remoteId = if (isNull(10)) null else getString(10),
        createdAt = getLong(11)
    )

    private fun getCategoriesInternal(): List<Category> {
        val list = mutableListOf<Category>()

        dbHelper.readableDatabase.rawQuery(
            "SELECT $CAT_ID, $CAT_NAME, $CAT_EMOJI FROM $TBL_CATEGORIES",
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
    suspend fun getCategoryByName(name: String): Category? = withContext(Dispatchers.IO) {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT $CAT_ID, $CAT_NAME, $CAT_EMOJI FROM $TBL_CATEGORIES WHERE $CAT_NAME = ?",
            arrayOf(name)
        )

        var category: Category? = null

        cursor.use {
            if (it.moveToFirst()) {
                category = Category(
                    id = it.getLong(0),
                    name = it.getString(1),
                    emoji = it.getString(2) ?: ""
                )
            }
        }

        category
    }
}