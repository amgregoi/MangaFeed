package com.amgregoire.mangafeed.v2.database

import androidx.room.*
import com.amgregoire.mangafeed.Models.Manga


@Dao
interface MangaDao
{

    @get:Query("SELECT * FROM Manga")
    val all: List<Manga>

    @Query("SELECT * FROM Manga where _id LIKE :mangaId")
    fun findById(mangaId: Int): Manga

    @Query("SELECT * FROM Manga where link LIKE :url AND source LIKE :source")
    fun findByUrl(url: String, source: String): Manga

    @Query("SELECT * FROM Manga where source LIKE :source")
    fun findAllBySource(source: String): List<Manga>

    @Query("SELECT * FROM Manga")
    fun findAll(): List<Manga>

    @Query("SELECT * FROM Manga where source LIKE :source AND following != 0")
    fun findFollowed(source: String): List<Manga>

    @Query("SELECT * FROM Manga where source LIKE :source AND following LIKE :followStatus")
    fun findFollowedWithFilter(source: String, followStatus:Int): List<Manga>

    @Query("SELECT COUNT(*) from Manga")
    fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg manga: Manga)

    @Delete
    fun delete(user: Manga)
}