package com.amgregoire.mangafeed.v2.di.module

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


/** */
@Module
class DatabaseModule @Inject
constructor(var dbImpl: MyDatabase)
{

    val database: MyDatabase
        @Provides
        @Singleton
        get() = dbImpl
}

private val DATABASE_VERSION = 1
private val DB_NAME = "MangaFeed.db"

@Singleton
class MyDatabase @Inject constructor(@Named("ApplicationContext") context: Context) // more code
    : SQLiteOpenHelper(context, DB_NAME, null, DATABASE_VERSION)
{
    override fun onCreate(p0: SQLiteDatabase?)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}