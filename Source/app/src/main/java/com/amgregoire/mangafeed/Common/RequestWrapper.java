package com.amgregoire.mangafeed.Common;

import android.os.Parcel;
import android.os.Parcelable;

import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;


public class RequestWrapper implements Parcelable
{
    public static final String TAG = RequestWrapper.class.getSimpleName();

    private Manga mManga;
    private Chapter mChapter;

    /***
     * Request Wrapper Constructor
     *
     * @param aManga
     */
    public RequestWrapper(Manga aManga)
    {
        mManga = aManga;
    }

    /***
     * Request Wrapper Constructor
     *
     * @param aChapter
     */
    public RequestWrapper(Chapter aChapter)
    {
        mChapter = aChapter;
    }

    private RequestWrapper(Parcel aIn)
    {
        mManga = aIn.readParcelable(ClassLoader.getSystemClassLoader());
    }

    /***
     * This function returns the manga item source
     *
     * @return
     */
    public String getSource()
    {
        return mManga.getSource();
    }

    public Manga getManga()
    {
        return mManga;
    }

    public Chapter getChapter()
    {
        return mChapter;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel aOut, int aFlags)
    {
        aOut.writeParcelable(mManga, 0);
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