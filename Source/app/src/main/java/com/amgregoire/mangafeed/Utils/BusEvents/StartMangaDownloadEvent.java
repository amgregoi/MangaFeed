package com.amgregoire.mangafeed.Utils.BusEvents;

import com.amgregoire.mangafeed.Models.Chapter;

import java.util.List;

/**
 * Created by Andy Gregoire on 3/13/2018.
 */

public class StartMangaDownloadEvent
{
    public List<Chapter> chapters;

    public StartMangaDownloadEvent(List<Chapter> list)
    {
        chapters = list;
    }
}
