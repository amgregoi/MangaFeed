package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.UI.Adapters.DownloadsViewPagerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.Utils.MangaLogger;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class DownloadsPres implements IDownloads.DownloadsPres
{
    public final static String TAG = DownloadsPres.class.getSimpleName();

    private IDownloads.DownloadsMap mMap;
    private DownloadsViewPagerAdapter mAdapter;
    private FragmentManager mManager;

    public DownloadsPres(IDownloads.DownloadsMap map, FragmentManager manager)
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
                mAdapter = new DownloadsViewPagerAdapter(mManager, 3);
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
