package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.UI.Adapters.HomeViewPagerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.MangaLogger;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomePres implements IHome.HomePres
{
    public final static String TAG = HomePres.class.getSimpleName();

    private IHome.HomeMap mMap;
    private HomeViewPagerAdapter mAdapter;
    private FragmentManager mManager;

    public HomePres(IHome.HomeMap map, FragmentManager manager)
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

            if (mAdapter == null)
            {
                mAdapter = new HomeViewPagerAdapter(mManager, 3);
            }

            mMap.registerAdapter(mAdapter);
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public Fragment getAdapterFragment(int position)
    {
        return mAdapter.getRegisteredFragment(position);
    }
}
