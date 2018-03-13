package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Manga;

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
public class UpdateFollowStatusEvent
{
    public Manga manga;

    public UpdateFollowStatusEvent(Manga m)
    {
        manga = m;
    }
}
