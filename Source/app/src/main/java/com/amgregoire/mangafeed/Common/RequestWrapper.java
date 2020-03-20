package com.amgregoire.mangafeed.Common;

import android.os.Parcel;
import android.os.Parcelable;

import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;


public class RequestWrapper implements Parcelable
{
    public static final String TAG = RequestWrapper.class.getSimpleName();

    private DbManga mDbManga;
    private DbChapter mDbChapter;

    /***
     * Request Wrapper Constructor
     *
     * @param aDbManga
     */
    public RequestWrapper(DbManga aDbManga)
    {
        mDbManga = aDbManga;
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

    private RequestWrapper(Parcel aIn)
    {
        mDbManga = aIn.readParcelable(ClassLoader.getSystemClassLoader());
    }

    /***
     * This function returns the manga item source
     *
     * @return
     */
    public String getSource()
    {
        return mDbManga.getSource();
    }

    public DbManga getManga()
    {
        return mDbManga;
    }

    public DbChapter getChapter()
    {
        return mDbChapter;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel aOut, int aFlags)
    {
        aOut.writeParcelable(mDbManga, 0);
    }

    public static final Parcelable.Creator<RequestWrapper> CREATOR = new Parcelable.Creator<RequestWrapper>()
    {
        @Override
        public RequestWrapper createFromParcel(Parcel aInputParcel)
        {
            return new RequestWrapper(aInputParcel);
        }

        @Override
        public RequestWrapper[] newArray(int aSize)
        {
            return new RequestWrapper[aSize];
        }
    };
}