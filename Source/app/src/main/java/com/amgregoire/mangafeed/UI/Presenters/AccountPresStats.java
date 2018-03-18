package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentFiltered;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateSourceEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountPresStats implements IAccount.AccountStatsPres
{
    public final static String TAG = AccountPresStats.class.getSimpleName();

    private IAccount.AccountStatsMap mMap;
    private FragmentManager mManager;

    private Disposable mRxBus;
    private List<Long> mStatValues;

    public AccountPresStats(IAccount.AccountStatsMap map, FragmentManager manager)
    {
        mMap = map;
        mManager = manager;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();
            getFollowStats();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }


    @Override
    public void unSubEventBus()
    {
        try
        {
            mRxBus.dispose();
            mRxBus = null;
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void subEventBus()
    {
        try
        {
            mRxBus = MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
            {
                if (o instanceof UpdateMangaItemViewEvent)
                {
                    getFollowStats();
                }
                else if (o instanceof UpdateSourceEvent)
                {
                    mMap.initViews();
                    getFollowStats();
                }
            }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void startFilterFragment(int filter, String title)
    {
        try
        {
            if (mStatValues.get(filter - 1) > 0) // Filters are 1 indexed, stat list is 0 indexed
            {
                Fragment lFragment = AccountFragmentFiltered.newInstance(filter, title);
                mManager.beginTransaction()
                        .add(android.R.id.content, lFragment)
                        .addToBackStack(null)
                        .commit();
            }
            else
            {
                MangaFeed.getInstance().makeToastShort("You have no items in this section");
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function retrieves the counts of each filter type from the devices local database.
     *
     */
    private void getFollowStats()
    {
        try
        {
            mStatValues = new ArrayList<>();
            MangaDB.getInstance()
                   .getLibraryFilterCount(1, 2, 3, 4)
                   .cache()
                   .subscribe(aLong -> mStatValues.add(aLong),
                           throwable -> MangaLogger.logError(TAG, throwable.getMessage()),
                           () -> mMap.setFollowStats(mStatValues));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

}
