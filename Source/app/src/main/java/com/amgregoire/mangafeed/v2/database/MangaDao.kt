package com.amgregoire.mangafeed.v2.database

import androidx.room.*
import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.v2.model.domain.Manga


@Dao
interface MangaDao
{

    @get:Query("SELECT * FROM Manga")
    val all: List<DbManga>

    @Query("SELECT * FROM Manga where id LIKE :mangaId")
    fun findById(mangaId: String): DbManga

    @Query("SELECT * FROM Manga where link LIKE :url AND source LIKE :source")
    fun findByUrl(url: String, source: String): DbManga

    @Query("SELECT * FROM Manga where source LIKE :source")
    fun findAllBySource(source: String): List<DbManga>

    @Query("SELECT * FROM Manga")
    fun findAll(): List<DbManga>

    @Query("SELECT * FROM Manga where source LIKE :source AND following != 0")
    fun findFollowed(source: String): List<DbManga>

    @Query("SELECT * FROM Manga where source LIKE :source AND following LIKE :followStatus")
    fun findFollowedWithFilter(source: String, followStatus: Int): List<DbManga>

    @Query("SELECT * FROM Manga where source LIKE :source AND link IN (:urls)")
    fun test(source: String, urls: List<String>): List<DbManga>

    @Query("SELECT COUNT(*) from Manga")
    fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg dbDbManga: DbManga)

    @Query("SELECT * FROM Manga WHERE source LIKE :source AND link IN (:links)")
    fun queryObjects(source: String, links: List<String>): List<DbManga>

    @Delete
    fun delete(user: DbManga)

    @Query("UPDATE Manga SET following = 0 WHERE following > 0")
    fun resetLibrary()

}