package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Manga;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

public class MangaSelectedEvent
{
    public Manga manga;

    public MangaSelectedEvent(Manga m)
    {
        manga = m;
    }
}
