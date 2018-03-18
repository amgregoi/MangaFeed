package com.amgregoire.mangafeed.UI.Presenters;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;


/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomePresRecent extends HomePresBase
{
    public final static String TAG = HomePresRecent.class.getSimpleName();

    private boolean mIsInitialized = false;

    public HomePresRecent(IHome.HomeBaseMap map)
    {
        super(map);
    }

    @Override
    public void updateMangaList()
    {
        try
        {
            if (mMangaListSubscription != null)
            {
                mMangaListSubscription.dispose();
                mMangaListSubscription = null;
            }


            mMap.startRefresh();
            mMangaListSubscription = MangaFeed.getInstance()
                                              .getCurrentSource()
                                              .getRecentMangaObservable()
                                              .cache()
                                              .subscribe(mangas ->
                                              {
                                                  // OnNext
                                                  updateMangaGridView(mangas);
                                                  mIsInitialized = true;
                                              }, throwable ->
                                              {
                                                  // OnError
                                                  mIsInitialized = false;

                                              }, () ->
                                              {
                                                  // OnComplete
                                                  mMangaListSubscription.dispose();
                                              });
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function lets the recent fragment know when the device has reconnected to the internet.
     * It will update the manga list if the view has not already been initialized.
     *
     */
    public void hasInternetMessage()
    {
        try
        {
            if (!mIsInitialized)
            {
                updateMangaList();
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function updates the manga list when the the user interacts with the swipe refresh layout.
     *
     */
    public void onSwipeRefresh()
    {
        try
        {
            if (mAdapter != null)
            {
                mAdapter.updateOriginalData(new ArrayList<>());
            }

            updateMangaList();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }


}
