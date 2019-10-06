package com.amgregoire.mangafeed.v2.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.v2.di.ApplicationContext
import com.amgregoire.mangafeed.v2.di.DatabaseInfo
import dagger.Module
import dagger.Provides


@Module
class ApplicationModule(private val application: Application)
{

    @Provides
    @ApplicationContext
    internal fun provideContext(): Context
    {
        return application
    }

    @Provides
    internal fun provideApplication(): Application
    {
        return application
    }

    @Provides
    @DatabaseInfo
    internal fun provideDatabaseName(): String
    {
        return "demo-dagger.db"
    }

    @Provides
    @DatabaseInfo
    internal fun provideDatabaseVersion(): Int
    {
        return 2
    }

    @Provides
    internal fun provideSharedPrefs(): SharedPreferences
    {
        return application.getSharedPreferences("demo-prefs", Context.MODE_PRIVATE)
    }

    @Provides
    internal fun provideMangaDB(): MangaDB
    {
        return MangaDB(application, provideDatabaseName(), provideDatabaseVersion())
    }
}