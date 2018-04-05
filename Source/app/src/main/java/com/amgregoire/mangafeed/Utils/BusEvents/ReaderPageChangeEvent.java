package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 4/4/2018.
 */

public class ReaderPageChangeEvent
{
    public boolean isNext;

    public ReaderPageChangeEvent(boolean next)
    {
        isNext = next;
    }
}
