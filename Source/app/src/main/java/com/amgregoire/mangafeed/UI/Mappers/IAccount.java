package com.amgregoire.mangafeed.UI.Mappers;

import com.amgregoire.mangafeed.UI.Mappers.Maps.ContextMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.EventBusMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.InitViewMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.LifeCycleMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.PagerAdapterMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.RecycleAdapterMap;

import java.util.List;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public interface IAccount
{
    /***
     * Presenter interfaces for the Home Fragments to communicate with their presenters.
     *
     */
    interface AccountPres extends LifeCycleMap, EventBusMap
    {

    }

    interface AccountSourcePres extends LifeCycleMap
    {

    }

    interface AccountStatsPres extends LifeCycleMap, EventBusMap
    {
        void startFilterFragment(int filter, String title);
    }

    interface AccountFilteredPres extends LifeCycleMap, EventBusMap
    {

    }

    /***
     * Mapper interfaces for the Home presenters to communicate with their views.
     *
     */
    interface AccountMap extends PagerAdapterMap, InitViewMap, ContextMap
    {
        void setHeaderUserName();
    }

    interface AccountSourceMap extends RecycleAdapterMap, InitViewMap, ContextMap
    {

    }

    interface AccountStatsMap extends InitViewMap, ContextMap
    {
        void setFollowStats(List<Long> values);
    }

    interface AccountFilteredMap extends RecycleAdapterMap, InitViewMap, ContextMap
    {

    }
}
