package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.NetworkService;

import java.util.ArrayList;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomePresRecent extends HomePresBase
{
    public final static String TAG = HomePresRecent.class.getSimpleName();

    public HomePresRecent(IHome.HomeBaseMap map)
    {
        super(map);
    }

    @Override
    public void updateMangaList()
    {
        if (mMangaListSubscription != null)
        {
            mMangaListSubscription.unsubscribe();
            mMangaListSubscription = null;
        }

        try
        {
            if(mAdapter != null)
            {
                mAdapter.updateOriginalData(new ArrayList<>());
            }

            mMap.startRefresh();
            mMangaListSubscription = MangaFeed.getInstance().getCurrentSource()
                                              .getRecentMangaObservable()
                                              .cache()
                                              .subscribe(aManga -> updateMangaGridView(aManga));

        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());

        }
    }

}
