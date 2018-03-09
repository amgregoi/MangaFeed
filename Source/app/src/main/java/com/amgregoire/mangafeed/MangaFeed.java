package com.amgregoire.mangafeed;

import android.app.Application;
import android.widget.Toast;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class MangaFeed extends Application
{
    private static MangaFeed mInstance;

    public MangaFeed()
    {
        mInstance = this;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        MangaDB.getInstance().createDB(); // Copy pre-loaded database if not already done.
        MangaDB.getInstance().initDao();
    }

    /***
     * This function returns the MangaFeed application instance
     * @return
     */
    public static synchronized MangaFeed getInstance()
    {
        return mInstance;
    }

    /***
     * This function creates and shows a SHORT toast message.
     *
     * @param message
     */
    public void makeToastShort(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /***
     * This function creates and shows a LONG toast message.
     *
     * @param message
     */
    public void makeToastLong(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    /***
     * This function returns the current sources source type
     *
     * NOVEL
     * MANGA
     *
     * @return
     */
    public MangaEnums.SourceType getCurrentSourceType()
    {
        return MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).getSource().getSourceType();
    }

    /***
     * This function returns the current source.
     *
     * MangaEden
     * MangaHere
     * MangaJoy
     * ReadLight
     *
     * @return
     */
    public SourceBase getCurrentSource()
    {
        return MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).getSource();
    }


}
