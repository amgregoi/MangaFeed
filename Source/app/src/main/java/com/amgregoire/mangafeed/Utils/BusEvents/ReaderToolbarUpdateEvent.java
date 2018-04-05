package com.amgregoire.mangafeed.Utils.BusEvents;

/**
 * Created by Andy Gregoire on 4/4/2018.
 */

public class ReaderToolbarUpdateEvent
{
    public String message;
    public String currentPage;
    public String totalPages;

    public int chapterPosition;

    public ReaderToolbarUpdateEvent(String msg, int page, int total, int chPosition)
    {
        message = msg;
        currentPage = "" + page;
        totalPages = "" + total;
        chapterPosition = chPosition;
    }
}
