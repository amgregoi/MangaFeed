package com.amgregoire.mangafeed.v2.repository.local

import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.enums.FilterType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.model.mappers.DatabaseMangaMapper
import kotlinx.coroutines.launch

class LocalCatalogRepository(
        private val mangaMapper: DatabaseMangaMapper = DatabaseMangaMapper()
)
{
    val database = MangaDB.getInstance().database

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
}