package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Manga;

/**
 * Created by Andy Gregoire on 3/10/2018.
 */

public class UpdateItemEvent
{
    public Manga manga;

    public UpdateItemEvent(Manga m)
    {
        manga = m;
    }
}
