package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.DbChapter;

import java.util.List;

/**
 * Created by Andy Gregoire on 3/13/2018.
 */

/***
 * This class represents an event that is fired when the user selects an item(s) to be downloaded.
 *
 * Triggered in:
 * MangaInfoFragment
 *
 * Consumed in:
 * DownloadsFragment
 *
 */
public class StartDownloadEvent
{
    public List<DbChapter> dbChapters;

    public StartDownloadEvent(List<DbChapter> list)
    {
        dbChapters = list;
    }
}
