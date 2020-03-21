package com.amgregoire.mangafeed.Common;

import android.os.Parcel;
import android.os.Parcelable;

import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.v2.model.domain.Manga;


public class RequestWrapper
{
    public static final String TAG = RequestWrapper.class.getSimpleName();

    private Manga manga;
    private DbChapter mDbChapter;

    /***
     * Request Wrapper Constructor
     *
     * @param manga
     */
    public RequestWrapper(Manga manga)
    {
        this.manga = manga;
    }

    /***
     * Request Wrapper Constructor
     *
     * @param aDbChapter
     */
    public RequestWrapper(DbChapter aDbChapter)
    {
        mDbChapter = aDbChapter;
    }

    /***
     * This function returns the manga item source
     *
     * @return
     */
    public String getSource()
    {
        return manga.getSource();
    }

    public Manga getManga()
    {
        return manga;
    }

    public DbChapter getChapter()
    {
        return mDbChapter;
    }
}