package com.amgregoire.mangafeed.v2.repository.local

import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.v2.database.AppDatabase

class LocalChapterRepository(
        private val database: AppDatabase = MangaDB.getInstance().database
)
{

    /***
     * This function adds or updates a chapter object in the local database.
     *
     * @param dbChapter the Chapter to be added, or updated.
     */
    fun putChapter(dbChapter: DbChapter?)
    {
        database.chapterDao().insertAll(dbChapter!!)
    }

    /***
     * This function retrieves a unique chapter object from the local database specified by its url.
     *
     * @param url the URL of the chapter.
     * @return
     */
    fun getChapter(url: String?): DbChapter?
    {
        return database.chapterDao().findByUrl(url!!)
    }

    fun getChapter(id: Int): DbChapter?
    {
        return database.chapterDao().findById(id)
    }


    /***
     * This function retrieves a locally created chapter with the stored version in the local database.
     * If there is no entry for the chapter, one is made and returned.
     *
     * @param dbChapter
     * @return
     */
    fun getChapter(dbChapter: DbChapter): DbChapter?
    {
        return getChapter(dbChapter.url)
    }


}