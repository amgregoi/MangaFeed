package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 4/22/2018.
 */

public class ToolbarTimerEvent
{
    public boolean isStart;
    public int chapterPosition;

    public ToolbarTimerEvent(boolean start, int position)
    {
        isStart = start;
        chapterPosition = position;
    }
}
