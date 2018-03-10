package com.amgregoire.mangafeed.UI.Presenters;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

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
        if (mMangaListSubscription != null)
        {
            mMangaListSubscription.unsubscribe();
            mMangaListSubscription = null;
        }

        try
        {
            mMap.startRefresh();
            mMangaListSubscription = MangaFeed.getInstance().getCurrentSource()
                                              .getRecentMangaObservable()
                                              .cache()
                                              .subscribe(new Subscriber<List<Manga>>()
                                              {
                                                  @Override
                                                  public void onCompleted()
                                                  {

                                                  }

                                                  @Override
                                                  public void onError(Throwable e)
                                                  {
                                                      // Failed to initialize
                                                      mIsInitialized = false;
                                                  }

                                                  @Override
                                                  public void onNext(List<Manga> mangas)
                                                  {
                                                      updateMangaGridView(mangas);
                                                      mIsInitialized = true;
                                                  }
                                              });
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }

    /***
     * This function lets the recent fragment know when the device has reconnected to the internet.
     * It will update the manga list if the view has not already been initialized.
     *
     */
    public void hasInternetMessage()
    {
        if(!mIsInitialized)
        {
            updateMangaList();
        }
    }

    /***
     * This function updates the manga list when the the user interacts with the swipe refresh layout.
     *
     */
    public void onSwipeRefresh()
    {
        if (mAdapter != null)
        {
            mAdapter.updateOriginalData(new ArrayList<>());
        }

        updateMangaList();
    }


}
