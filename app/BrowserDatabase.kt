package com.nextbrowser.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "history",
    indices = [Index(value = ["url"], unique = false)]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface BrowserDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 500")
    fun history(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun bookmarks(): Flow<List<BookmarkEntity>>

    @Insert
    suspend fun addHistory(item: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(item: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(item: BookmarkEntity)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmarks()

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    suspend fun isBookmarked(url: String): Boolean

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun removeBookmark(url: String)
}

@Database(
    entities = [HistoryEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun dao(): BrowserDao

    companion object {
        @Volatile private var INSTANCE: BrowserDatabase? = null
        fun get(context: Context): BrowserDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "next_browser.db"
                ).build().also { INSTANCE = it }
            }
    }
}
