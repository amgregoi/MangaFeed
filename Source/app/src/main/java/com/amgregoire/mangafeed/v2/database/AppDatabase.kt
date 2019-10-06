package com.amgregoire.mangafeed.v2.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga


@Database(entities = [Manga::class, Chapter::class], version = 1)
abstract class AppDatabase : RoomDatabase()
{

    abstract fun chapterDao(): ChapterDao
    abstract fun mangaDao(): MangaDao

    companion object
    {

        private var INSTANCE: AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase
        {
            if (INSTANCE == null)
            {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "MangaFeed.db")
                        // allow queries on the main thread.
                        // Don't do this on a real app! See PersistenceBasicSample for an example.
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE as AppDatabase
        }

        fun destroyInstance()
        {
            INSTANCE = null
        }
    }
}