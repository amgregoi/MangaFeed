package com.amgregoire.mangafeed.Common;


import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase;
import com.amgregoire.mangafeed.Common.WebSources.FunManga;
import com.amgregoire.mangafeed.Common.WebSources.MangaEden;
import com.amgregoire.mangafeed.Common.WebSources.MangaHere;
import com.amgregoire.mangafeed.Common.WebSources.ReadLight;
import com.amgregoire.mangafeed.Common.WebSources.Wuxia;
import com.amgregoire.mangafeed.R;

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

        @Override
        public String toString()
        {
            return lSource.getSourceName();
        }

        public static int getPosition(String sourceName)
        {
            Source[] array = values();
            for(int i =0; i< array.length; i++)
            {
                if(array[i].lSource.getSourceName().equalsIgnoreCase(sourceName)) return i;
            }

            return 0;
        }
    }

    /***
     * This enum is for the various follow status types.
     */
    public enum FollowType
    {
        Unfollow(0, R.string.manga_info_header_fab_follow, R.drawable.ic_heart_outline_white_24dp),
        Reading(1, R.string.manga_info_header_fab_reading, R.drawable.ic_heart_white_24dp),
        Completed(2, R.string.manga_info_header_fab_complete, R.drawable.ic_heart_white_24dp),
        On_Hold(3, R.string.manga_info_header_fab_on_hold, R.drawable.ic_heart_white_24dp),
        Plan_to_Read(4, R.string.manga_info_header_fab_plan_to_read, R.drawable.ic_heart_white_24dp);

        public int value;
        public int stringRes;
        public int drawableRes;

        FollowType(int value, int stringRes, int drawableRes)
        {
            this.value = value;
            this.stringRes = stringRes;
            this.drawableRes = drawableRes;
        }

        @Override
        public String toString()
        {
            return super.toString().replace("_", " ");
        }
    }
}
