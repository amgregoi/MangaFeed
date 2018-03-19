package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 3/18/2018.
 */

public class SearchQueryChangeEvent
{
    public String query;

    public SearchQueryChangeEvent(String s)
    {
        query = s;
    }
}
