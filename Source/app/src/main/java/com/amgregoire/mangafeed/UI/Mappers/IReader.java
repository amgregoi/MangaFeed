package com.amgregoire.mangafeed.UI.Mappers;

import com.amgregoire.mangafeed.UI.Mappers.Maps.ContextMap;
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
    }

    interface ReaderMapChapter extends PagerAdapterMap, ContextMap, InitViewMap, GestureViewPager.UserGestureListener
    {

    }

    interface ReaderPres extends LifeCycleMap
    {

    }

    interface ReaderPresChapter extends LifeCycleMap
    {

    }
}
