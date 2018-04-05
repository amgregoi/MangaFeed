package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 4/4/2018.
 */

public class ReaderChapterChangeEvent
{
    public boolean isNext;

    public ReaderChapterChangeEvent(boolean next)
    {
        isNext = next;
    }
}
