package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Manga;

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
    public Manga manga;
    public boolean isOffline;

    public MangaSelectedEvent(Manga m, boolean offline)
    {
        manga = m;
        isOffline = offline;
    }
}
