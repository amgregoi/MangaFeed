package com.amgregoire.mangafeed.UI.Mappers;

import android.support.v4.app.Fragment;

import com.amgregoire.mangafeed.UI.Mappers.Maps.ContextMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.EventBusMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.InitViewMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.LifeCycleMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.PagerAdapterMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.RecycleAdapterMap;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public interface IHome
{
    /***
     * Presenter interfaces for the Home Fragments to communicate with their presenters.
     *
     */
    interface HomePres extends LifeCycleMap
    {
        Fragment getAdapterFragment(int position);
    }

    interface HomeBasePres extends LifeCycleMap, EventBusMap
    {
        void updateMangaList();
    }

    /***
     * Mapper interfaces for the Home presenters to communicate with their views.
     *
     */
    interface HomeMap extends PagerAdapterMap, InitViewMap, ContextMap
    {

    }

    interface HomeBaseMap extends RecycleAdapterMap, InitViewMap, ContextMap
    {
        //Swipe layout functions
        void startRefresh();
        void stopRefresh();
    }
}
