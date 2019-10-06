package com.amgregoire.mangafeed.v2.database

import androidx.room.*
import com.amgregoire.mangafeed.Models.Chapter

@Dao
interface ChapterDao
{
    @get:Query("SELECT * FROM Chapter")
    val all: List<Chapter>

    @Query("SELECT * FROM Chapter where _id LIKE :chapterId")
    fun findById(chapterId: Int): Chapter

    @Query("SELECT DISTINCT * FROM Chapter where url LIKE :url")
    fun findByUrl(url: String): Chapter

    @Query("SELECT * FROM Chapter where mangaUrl LIKE :url")
    fun findAllByMangaUrl(url: String): List<Chapter>

    @Query("SELECT COUNT(*) from Chapter")
    fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: Chapter)

    @Delete
    fun delete(user: Chapter)
}