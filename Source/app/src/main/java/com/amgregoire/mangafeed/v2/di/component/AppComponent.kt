package com.amgregoire.mangafeed.v2.di.component

import android.app.Application
import android.content.Context
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.di.module.AppContextModule
import dagger.Component
import com.amgregoire.mangafeed.v2.di.ApplicationContext
import com.amgregoire.mangafeed.v2.di.module.ApplicationModule
import javax.inject.Singleton


@Singleton
@Component(modules = [
    ApplicationModule::class,
    AppContextModule::class
])
interface AppComponent
{
    fun inject(mangaApp: MangaFeed)

    @ApplicationContext
    fun getContext(): Context

    fun getApplication(): Application

//    fun getDataManager(): MangaDB

    fun getPreferenceHelper(): SharedPrefs

//    fun getDbHelper(): DbHelper
}