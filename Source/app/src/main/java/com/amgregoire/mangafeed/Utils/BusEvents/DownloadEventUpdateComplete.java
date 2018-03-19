package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 3/19/2018.
 */

public class DownloadEventUpdateComplete
{
    String chapterUrl;

    public DownloadEventUpdateComplete(String url)
    {
        chapterUrl = url;
    }
}
