package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Manga;

/**
 * Created by Andy Gregoire on 3/13/2018.
 */

public class MangaDownloadSelectEvent
{
    public Manga manga;

    public MangaDownloadSelectEvent(Manga m)
    {
        manga = m;
    }
}
