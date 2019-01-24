package com.amgregoire.mangafeed.Common;


import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase;
import com.amgregoire.mangafeed.Common.WebSources.MangaEden;
import com.amgregoire.mangafeed.Common.WebSources.MangaHere;
import com.amgregoire.mangafeed.Common.WebSources.FunManga;
import com.amgregoire.mangafeed.Common.WebSources.ReadLight;
import com.amgregoire.mangafeed.Common.WebSources.Wuxia;

public class MangaEnums
{
    /***
     * This enum is for the various manga filter status'
     */
    public enum FilterStatus
    {
        NONE(0),

        READING(1),

        ON_HOLD(3),

        COMPLETE(2),

        FOLLOWING(5);

        private int mValue;

        FilterStatus(int aValue)
        {
            mValue = aValue;
        }

        public int getValue()
        {
            return mValue;
        }

    }


    /***
     * This enum is for the various loading status'
     */
    public enum LoadingStatus
    {
        COMPLETE,

        LOADING,

        ERROR,

        REFRESH;

        public static LoadingStatus getLoadingStatus(int aStatus)
        {
            switch (aStatus)
            {
                case 0:
                    return LOADING;
                case 1:
                    return COMPLETE;
                case 2:
                    return REFRESH;
                default:
                    return ERROR;
            }
        }

        public static int getLoadingStatus(LoadingStatus aStatus)
        {
            switch (aStatus)
            {
                case LOADING:
                    return 0;
                case COMPLETE:
                    return 1;
                case REFRESH:
                    return 2;
                default:
                    return 3;
            }
        }
    }

    public enum SourceType
    {
        MANGA,
        NOVEL
    }

    /***
     * This enum is for the various sources.
     */
    public enum Source
    {
        ReadLight(new ReadLight()),
        FunManga(new FunManga()),
        MangaHere(new MangaHere()),
        MangaEden(new MangaEden()),
        Wuxia(new Wuxia());

        SourceBase lSource;

        Source(SourceBase aSource)
        {
            lSource = aSource;
        }

        public SourceBase getSource()
        {
            return lSource;
        }

        public String getBaseUrl()
        {
            return lSource.getBaseUrl();
        }
    }

    /***
     * This enum is for the various follow status types.
     */
    public enum FollowType
    {
        Follow, Reading, Completed, On_Hold, Plan_to_Read;

        @Override
        public String toString()
        {
            return super.toString().replace("_", " ");
        }
    }
}
