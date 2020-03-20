package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.DbManga;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

/***
 * This class represents an event that is fired when a user interacts with a recyclerview item.
 * It is primarily triggered from
 *
 * Triggered in:
 * RecycleSearchAdapter
 *
 * Consumed in:
 * NavigationActivity
 */
public class MangaSelectedEvent
{
    public DbManga dbManga;
    public boolean isOffline;

    public MangaSelectedEvent(DbManga m, boolean offline)
    {
        dbManga = m;
        isOffline = offline;
    }
}
