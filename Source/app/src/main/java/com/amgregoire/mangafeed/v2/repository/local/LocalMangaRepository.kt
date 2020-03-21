package com.amgregoire.mangafeed.v2.repository.local

import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.currentSource
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.database.AppDatabase
import com.amgregoire.mangafeed.v2.enums.FilterType
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.model.mappers.DbMangaToMangaMapper
import com.amgregoire.mangafeed.v2.model.mappers.MangaToDbMangaMapper
import kotlinx.coroutines.launch

class LocalMangaRepository(
        private val mangaMapper: DbMangaToMangaMapper = DbMangaToMangaMapper(),
        private val database: AppDatabase = MangaDB.getInstance().database
)
{
    fun putManga(dbManga: DbManga): Manga?
    {
        database.mangaDao().insertAll(dbManga)
        return getManga(dbManga.link, dbManga.source)
    }

    fun putManga(manga: Manga): Manga?
    {
        val dbManga = MangaToDbMangaMapper().map(manga)
        database.mangaDao().insertAll(dbManga)
        return getManga(dbManga.link, dbManga.source)
    }

    fun updateManga(manga: Manga, followType: FollowType? = null): Manga?
    {
        followType?.let {
            manga.followType = followType
        }

        val dbManga = MangaToDbMangaMapper().map(manga)
        database.mangaDao().insertAll(dbManga)
        return getManga(dbManga.link, dbManga.source);
    }

    fun getManga(link: String): Manga?
    {
        val dbManga = database.mangaDao().findByUrl(link, currentSource.sourceName) ?: return null
        return mangaMapper.map(dbManga)
    }

    fun getManga(link: String, source: String): Manga?
    {
        val dbManga = database.mangaDao().findByUrl(link, source) ?: return null
        return mangaMapper.map(dbManga)
    }

    fun containsManga(link: String, source: String): Boolean
    {
        return getManga(link, source) != null
    }

    fun getExistingManga(urlList: List<String>, source: String): List<Manga>
    {
        return database.mangaDao().test(source, urlList).map { mangaMapper.map(it) }
    }


    /**
     * This function retrieves the list of followed items from the database.
     *
     * @return Observable arraylist of users followed manga
     */
    fun getLibrary(filter: FilterType, result: (List<Manga>) -> Unit) = ioScope.launch {
        val manga =
                if (filter == FilterType.NONE) database.mangaDao()
                        .findFollowed(SharedPrefs.getSavedSource())
                        .map { mangaMapper.map(it) }
                else database.mangaDao()
                        .findFollowedWithFilter(SharedPrefs.getSavedSource(), filter.value)
                        .map { mangaMapper.map(it) }

        uiScope.launch { result(manga) }
    }


    /**
     * This function retrieves the source catalog from the database.
     *
     * @return Observable arraylist of sources manga
     */
    fun getCatalogList(result: (List<Manga>) -> Unit) = ioScope.launch {
        val manga = database.mangaDao()
                .findAllBySource(SharedPrefs.getSavedSource())
                .map { mangaMapper.map(it) }

        uiScope.launch { result(manga) }
    }

    /***
     * This function resets the local database, and sets all following flags to 0.
     *
     */
    fun resetLibrary() = ioScope.launch {
        database.mangaDao().resetLibrary()
    }
}