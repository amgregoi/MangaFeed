package com.amgregoire.mangafeed.v2.database

import androidx.room.*
import com.amgregoire.mangafeed.Models.DbChapter

@Dao
interface ChapterDao
{
    @get:Query("SELECT * FROM Chapter")
    val all: List<DbChapter>

    @Query("SELECT * FROM Chapter where _id LIKE :chapterId")
    fun findById(chapterId: Int): DbChapter

    @Query("SELECT DISTINCT * FROM Chapter where url LIKE :url")
    fun findByUrl(url: String): DbChapter

    @Query("SELECT * FROM Chapter where mangaUrl LIKE :url")
    fun findAllByMangaUrl(url: String): List<DbChapter>

    @Query("SELECT COUNT(*) from Chapter")
    fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: DbChapter)

    @Delete
    fun delete(user: DbChapter)
}