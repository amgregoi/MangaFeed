package com.amgregoire.mangafeed.Utils;

import android.util.Log;
import android.widget.Toast;

import com.amgregoire.mangafeed.MangaFeed;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MangaLogger
{
    private final static String mApplication = MangaFeed.class.getSimpleName();

    /***
     * This function logs info to the console as well as to the in app logger.
     *
     * @param aTag
     * @param aMessage
     */
    public static void logInfo(String aTag, String aMessage)
    {
        String lMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
        String lMessage = "INFO >> " + MessageFormat.format("{0}.class >> {1}() > {2}", aTag, lMethod, aMessage);
        Log.i(mApplication, lMessage);
    }

    /***
     * This function logs errors to the console as well as to the in app logger.
     *
     * @param aTag
     * @param aError
     */
    public static void logError(String aTag, String aError)
    {
        String lMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
        String lMessage = "ERROR >> " + MessageFormat.format("{0}.class >> {1}() > {2}", aTag, lMethod, aError);
        Log.e(mApplication, lMessage);
    }

    /***
     * This function logs errors to the console as well as to the in app logger.
     *
     * @param aTag
     * @param aError
     * @param aExtra
     */
    public static void logError(String aTag, String aError, String aExtra)
    {
        String lMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
        String lMessage = "ERROR >> " + MessageFormat.format("{0}.class >> {1}() > {2} > {3}", aTag, lMethod, aExtra, aError);
        Log.e(mApplication, lMessage);
    }

    /***
     * This function logs debug info to the console as well as to the in app logger.
     * @param aTag
     * @param aMessage
     */
    public static void logDebug(String aTag, String aMessage)
    {
        String lMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
        String lMessage = "DEBUG >> " + MessageFormat.format("{0}.class >> {1}() > {2}", aTag, lMethod, aMessage);
        Log.i(mApplication, lMessage);
    }
}
