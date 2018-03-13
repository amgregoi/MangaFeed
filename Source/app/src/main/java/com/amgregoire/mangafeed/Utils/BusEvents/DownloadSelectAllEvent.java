package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Manga;

/**
 * Created by Andy Gregoire on 3/13/2018.
 */

/***
 * This class represents an event that is fired when a user interacts with the (un)select all button in the
 * MangaInfoFragment toolbar while the download view is enabled.
 *
 * Triggered in:
 * MangaInfoFragment
 *
 * Consumed in:
 * NavigationActivity
 *
 */
public class DownloadSelectAllEvent
{
    public Manga manga;

    public DownloadSelectAllEvent(Manga m)
    {
        manga = m;
    }
}
