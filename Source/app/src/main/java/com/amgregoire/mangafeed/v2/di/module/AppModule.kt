package com.amgregoire.mangafeed.v2.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.amgregoire.mangafeed.v2.di.ApplicationContext
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton


@Module(includes = [
    RoomModule::class,
    ApiModule::class
])
class ApplicationModule @Inject constructor(private val application: Application)
{
    @Inject lateinit var roomModule: RoomModule

    @Provides
    @ApplicationContext
    internal fun provideContext(): Context
    {
        return application
    }

    @Provides
    @Singleton
    internal fun provideApplication(): Application
    {
        return application
    }

    @Provides
    internal fun provideSharedPrefs(): SharedPreferences
    {
        return application.getSharedPreferences("demo-prefs", Context.MODE_PRIVATE)
    }

    //    @Provides
    //    internal fun provideMangaDB(): MangaDB
    //    {
    //        return MangaDB(application, provideDatabaseName(), provideDatabaseVersion())
    //    }
}