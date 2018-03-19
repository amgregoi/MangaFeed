package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 3/19/2018.
 */

public class DownloadEventUpdatePageCount
{
    String chapterUrl;

    public DownloadEventUpdatePageCount(String url)
    {
        chapterUrl = url;
    }
}
