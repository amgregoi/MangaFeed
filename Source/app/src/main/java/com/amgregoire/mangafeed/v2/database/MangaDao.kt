package com.amgregoire.mangafeed.v2.database

import androidx.room.*
import com.amgregoire.mangafeed.Models.DbManga


@Dao
interface MangaDao
{

    @get:Query("SELECT * FROM Manga")
    val all: List<DbManga>

    @Query("SELECT * FROM Manga where _id LIKE :mangaId")
    fun findById(mangaId: Int): DbManga

    @Query("SELECT * FROM Manga where link LIKE :url AND source LIKE :source")
    fun findByUrl(url: String, source: String): DbManga

    @Query("SELECT * FROM Manga where source LIKE :source")
    fun findAllBySource(source: String): List<DbManga>

    @Query("SELECT * FROM Manga")
    fun findAll(): List<DbManga>

    @Query("SELECT * FROM Manga where source LIKE :source AND following != 0")
    fun findFollowed(source: String): List<DbManga>

    @Query("SELECT * FROM Manga where source LIKE :source AND following LIKE :followStatus")
    fun findFollowedWithFilter(source: String, followStatus:Int): List<DbManga>

    @Query("SELECT * FROM Manga where source LIKE :source AND link IN (:urls)")
    fun test(source: String, urls:List<String>): List<DbManga>

    @Query("SELECT COUNT(*) from Manga")
    fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg dbManga: DbManga)

    @Delete
    fun delete(user: DbManga)
}