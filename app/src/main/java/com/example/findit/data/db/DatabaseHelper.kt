package com.example.findit.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * F2 — Pure SQLiteOpenHelper (NO Room).
 *
 * Schema:
 *   categories(id PK AI, name UNIQUE, emoji)
 *   items(id PK AI, ..., category_id  -> FK -> categories.id)
 *
 * Foreign keys are enabled in onConfigure() so the FK constraint is enforced
 * at the engine level (not just declared).
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "findit.db"
        const val DB_VERSION = 1

        // ---- categories ----
        const val TBL_CATEGORIES = "categories"
        const val CAT_ID = "id"
        const val CAT_NAME = "name"
        const val CAT_EMOJI = "emoji"

        // ---- items ----
        const val TBL_ITEMS = "items"
        const val ITEM_ID = "id"
        const val ITEM_TITLE = "title"
        const val ITEM_DESC = "description"
        const val ITEM_TYPE = "type"              // "Lost" | "Found"
        const val ITEM_CATEGORY_ID = "category_id"
        const val ITEM_LOCATION = "location"
        const val ITEM_DATE = "date"              // yyyy-MM-dd
        const val ITEM_CONTACT = "contact"
        const val ITEM_SOURCE = "source"          // "LOCAL" | "API"
        const val ITEM_REMOTE_ID = "remote_id"    // server-side id (nullable)
        const val ITEM_CREATED_AT = "created_at"  // epoch millis

        private const val SQL_CREATE_CATEGORIES = """
            CREATE TABLE $TBL_CATEGORIES (
                $CAT_ID    INTEGER PRIMARY KEY AUTOINCREMENT,
                $CAT_NAME  TEXT NOT NULL UNIQUE,
                $CAT_EMOJI TEXT NOT NULL DEFAULT ''
            );
        """

        private const val SQL_CREATE_ITEMS = """
            CREATE TABLE $TBL_ITEMS (
                $ITEM_ID          INTEGER PRIMARY KEY AUTOINCREMENT,
                $ITEM_TITLE       TEXT NOT NULL,
                $ITEM_DESC        TEXT NOT NULL DEFAULT '',
                $ITEM_TYPE        TEXT NOT NULL DEFAULT 'Lost',
                $ITEM_CATEGORY_ID INTEGER NOT NULL,
                $ITEM_LOCATION    TEXT NOT NULL DEFAULT '',
                $ITEM_DATE        TEXT NOT NULL DEFAULT '',
                $ITEM_CONTACT     TEXT NOT NULL DEFAULT '',
                $ITEM_SOURCE      TEXT NOT NULL DEFAULT 'LOCAL',
                $ITEM_REMOTE_ID   INTEGER,
                $ITEM_CREATED_AT  INTEGER NOT NULL,
                FOREIGN KEY ($ITEM_CATEGORY_ID)
                    REFERENCES $TBL_CATEGORIES($CAT_ID)
                    ON DELETE RESTRICT
            );
        """

        private const val SQL_INDEX_REMOTE =
            "CREATE UNIQUE INDEX idx_items_remote ON $TBL_ITEMS($ITEM_REMOTE_ID) WHERE $ITEM_REMOTE_ID IS NOT NULL;"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Required so the FOREIGN KEY clause is actually enforced.
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_CATEGORIES)
        db.execSQL(SQL_CREATE_ITEMS)
        db.execSQL(SQL_INDEX_REMOTE)
        seedCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple migration strategy for the assignment: drop & recreate.
        db.execSQL("DROP TABLE IF EXISTS $TBL_ITEMS;")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CATEGORIES;")
        onCreate(db)
    }

    /** Seed the parent table so the FK from items.category_id always resolves. */
    private fun seedCategories(db: SQLiteDatabase) {
        val seed = listOf(
            "ID Card" to "🆔",
            "Wallet" to "💳",
            "Electronics" to "📱",
            "Books" to "📚",
            "Bags" to "🎒",
            "Keys" to "🔑",
            "Others" to "📦"
        )
        seed.forEach { (name, emoji) ->
            val cv = ContentValues().apply {
                put(CAT_NAME, name)
                put(CAT_EMOJI, emoji)
            }
            db.insertWithOnConflict(
                TBL_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE
            )
        }
    }
}
