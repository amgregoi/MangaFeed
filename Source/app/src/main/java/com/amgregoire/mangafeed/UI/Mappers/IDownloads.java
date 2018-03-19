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

public interface IDownloads
{
    /***
     * Presenter interfaces for the Home Fragments to communicate with their presenters.
     *
     */
    interface DownloadsPres extends LifeCycleMap
    {
        Fragment getAdapterFragment(int position);
    }

    interface DownloadsSavedPres extends LifeCycleMap, EventBusMap
    {

    }

    interface DownloadsDownloadingPres extends LifeCycleMap
    {

    }


    /***
     * Mapper interfaces for the Home presenters to communicate with their views.
     *
     */
    interface DownloadsMap extends PagerAdapterMap, InitViewMap, ContextMap
    {

    }

    interface DownloadsSavedMap extends RecycleAdapterMap, InitViewMap, ContextMap
    {

    }

    interface DownloadsDownloadingMap extends RecycleAdapterMap, InitViewMap, ContextMap
    {

    }
}
