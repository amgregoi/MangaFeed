package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 4/7/2018.
 */

public class StatusFilterEvent
{
    public int filter;
    public String title;

    public StatusFilterEvent(int filter, String title)
    {
        this.filter = filter;
        this.title = title;
    }
}
