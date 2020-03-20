package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.DbManga;

/**
 * Created by Andy Gregoire on 3/13/2018.
 */

/***
 * This class represents an event that is fired when a user toggles the download view in the manga information fragment.
 *
 * Triggered in:
 * MangaInfoFragment
 *
 * Consumed in:
 * NavigationActivity
 *
 */
public class ToggleDownloadViewEvent
{
    public DbManga dbManga;

    public ToggleDownloadViewEvent(DbManga m)
    {
        dbManga = m;
    }
}
