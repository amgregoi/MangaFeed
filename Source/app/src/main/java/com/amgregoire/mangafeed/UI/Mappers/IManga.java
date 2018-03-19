package com.amgregoire.mangafeed.UI.Mappers;

import com.amgregoire.mangafeed.UI.Mappers.Maps.ContextMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.InitViewMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.LifeCycleMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.RecycleAdapterMap;
import com.amgregoire.mangafeed.UI.Mappers.Maps.SwipeRefreshMap;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public interface IManga
{
    /***
     * Presenter interfaces for the Home Fragments to communicate with their presenters.
     *
     */
    interface MangaPres extends LifeCycleMap
    {
        void onSelectAllOrNone(boolean isAll);

        void onRefreshInfo();

        void onDownloadCancel();

        void onDownloadDownload();

        void onDownloadRemove();

        void onDownloadViewEnabled();

        void onUpdateFollowStatus(int status);
    }

    /***
     * Mapper interfaces for the Home presenters to communicate with their views.
     *
     */
    interface MangaMap extends RecycleAdapterMap, InitViewMap, ContextMap, SwipeRefreshMap
    {
        void setBottomNavStartContinue(String readText);

        void setBottomNavFollowTitle(int followType);
    }
}
