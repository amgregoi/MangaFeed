package com.amgregoire.mangafeed.UI.Mappers;

import android.os.Bundle;

import com.amgregoire.mangafeed.UI.Mappers.Maps.ContextMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.EventBusMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.InitViewMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.LifeCycleMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.PagerAdapterMap;
import com.amgregoire.mangafeed.UI.Widgets.GestureViewPager;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public interface IReader
{
    interface ReaderMap extends PagerAdapterMap, InitViewMap, ContextMap
    {
        void setPagerPosition(int position);

        void onSingleTap();

        void onNextChapter();

        void onPrevChapter();

        void updateToolbars(String message, String page, String total, int position);

        void startToolbarTimer(int chPosition);

        void stopToolbarTimer(int chPosition);
    }

    interface ReaderMapChapter extends PagerAdapterMap, ContextMap, InitViewMap, GestureViewPager.UserGestureListener
    {
        void onNextPage();
        void onPrevPage();
    }

    interface ReaderPres extends LifeCycleMap, EventBusMap
    {
        String getMangaTitle();

        String getChapterTitle();

        void onSaveState(Bundle save);

        void onRestoreState(Bundle restore);

        void updateCurrentPosition(int position);

    }

    interface ReaderPresChapter extends LifeCycleMap, EventBusMap
    {
        void updateCurrentPosition(int position);

        void setNewChapterToolbar();
    }
}
