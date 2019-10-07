package com.amgregoire.mangafeed.v2.di.module

import android.content.Context
import com.amgregoire.mangafeed.Utils.MangaDB
import dagger.Provides
import javax.inject.Singleton
import com.amgregoire.mangafeed.v2.database.AppDatabase
import com.amgregoire.mangafeed.v2.database.ChapterDao
import com.amgregoire.mangafeed.v2.database.MangaDao
import com.amgregoire.mangafeed.v2.di.DatabaseInfo
import com.amgregoire.mangafeed.v2.di.module.RoomModule.DatabaseInformation.databaseName
import com.amgregoire.mangafeed.v2.di.module.RoomModule.DatabaseInformation.databaseVersion
import dagger.Module
import javax.inject.Inject

@Module
class RoomModule(appContext: Context)
{
    private val database: AppDatabase = AppDatabase.getAppDatabase(appContext)
//    @Inject lateinit var mangaDB: MangaDB

    object DatabaseInformation
    {
        const val databaseName = "MangaFeed.db"
        const val databaseVersion = 1
    }

    @Provides
    @DatabaseInfo
    internal fun provideDatabaseName(): String
    {
        return databaseName
    }

    @Provides
    @DatabaseInfo
    internal fun provideDatabaseVersion(): Int
    {
        return databaseVersion
    }

    @Singleton
    @Provides
    fun providesRoomDatabase(): AppDatabase
    {
        return database
    }

    @Singleton
    @Provides
    fun providesMangaDao(demoDatabase: AppDatabase): MangaDao
    {
        return demoDatabase.mangaDao()
    }

    @Singleton
    @Provides
    fun providesChapterDao(demoDatabase: AppDatabase): ChapterDao
    {
        return demoDatabase.chapterDao()
    }
}
