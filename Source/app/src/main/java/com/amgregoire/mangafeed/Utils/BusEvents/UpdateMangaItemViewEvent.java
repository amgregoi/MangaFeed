package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.DbManga;

/**
 * Created by Andy Gregoire on 3/10/2018.
 */


/***
 * This class represents an event that is fired when a user changes a mangas follow status.
 * The event will update the manga in the views the event is consumed in.
 *
 * Triggered in:
 * RecyclerSearchAdapter (Header View)
 *
 * Consumed in:
 * HomeFragmentBase
 * HomeFragmentLibrary
 * AccountFragmentFiltered
 *
 */
public class UpdateMangaItemViewEvent
{
    public DbManga dbManga;
    public boolean isMulti;

    public UpdateMangaItemViewEvent(DbManga m)
    {
        dbManga = m;
        isMulti = false;
    }

    public UpdateMangaItemViewEvent()
    {
        isMulti = true;
    }
}
