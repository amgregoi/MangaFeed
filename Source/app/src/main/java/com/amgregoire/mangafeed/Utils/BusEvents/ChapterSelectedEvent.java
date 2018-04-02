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
public class ChapterSelectedEvent
{
    public Manga manga;
    public int position;

    public ChapterSelectedEvent(Manga m, int pos)
    {
        manga = m;
        position = pos;
    }
}
