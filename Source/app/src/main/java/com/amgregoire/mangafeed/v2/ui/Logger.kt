package com.amgregoire.mangafeed.v2.ui

import android.util.Log

import com.amgregoire.mangafeed.MangaFeed

import java.text.MessageFormat


object Logger
{
    private val mApplication = MangaFeed::class.java.simpleName

    /***
     * This function logs info to the console as well as to the in app logger.
     *
     * @param aTag
     * @param message
     */
    fun info(message: String)
    {
        val clazz = Thread.currentThread().stackTrace[3].className
        val method = Thread.currentThread().stackTrace[3].methodName
        val lMessage = "INFO >> " + MessageFormat.format("{0}.class >> {1}() > {2}", clazz, method, message)
        Log.i(mApplication, lMessage)
    }

    /***
     * This function logs debug info to the console as well as to the in app logger.
     * @param aTag
     * @param aMessage
     */
    fun debug(message: String)
    {
        val method = Thread.currentThread().stackTrace[3].methodName
        val clazz = Thread.currentThread().stackTrace[3].className
        val lMessage = "DEBUG >> " + MessageFormat.format("{0}.class >> {1}() > {2}", clazz, method, message)
        Log.i(mApplication, lMessage)
    }

    fun error(error: Throwable, extra: String = "") = error(error.localizedMessage, extra)

    fun error(error: String, extra: String = "")
    {
        val method = Thread.currentThread().stackTrace[3].methodName
        val clazz = Thread.currentThread().stackTrace[3].className
        val lMessage =
                if (extra.isEmpty()) "ERROR >> " + MessageFormat.format("{0}.class >> {1}() > {3}", clazz, method, extra, error)
                else "ERROR >> " + MessageFormat.format("{0}.class >> {1}() > {2} > {3}", clazz, method, extra, error)

        Log.e(mApplication, lMessage)
    }
}
