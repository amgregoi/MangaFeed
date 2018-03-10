package com.amgregoire.mangafeed.UI.Mappers;

import com.amgregoire.mangafeed.UI.Mappers.Maps.ContextMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.InitViewMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.LifeCycleMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.PagerAdapterMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.RecycleAdapterMap;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public interface IAccount
{
    /***
     * Presenter interfaces for the Home Fragments to communicate with their presenters.
     *
     */
    interface AccountPres extends LifeCycleMap
    {

    }

    interface AccountSourcePres extends LifeCycleMap
    {

    }

    interface AccountStatsPres extends LifeCycleMap
    {

    }

    /***
     * Mapper interfaces for the Home presenters to communicate with their views.
     *
     */
    interface AccountMap extends PagerAdapterMap, InitViewMap, ContextMap
    {

    }

    interface AccountSourceMap extends RecycleAdapterMap, InitViewMap, ContextMap
    {

    }

    interface AccountStatseMap extends InitViewMap, ContextMap
    {

    }
}
