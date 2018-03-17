package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.UI.Adapters.DownloadsViewPagerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class DownloadsPres implements IDownloads.DownloadsPres
{
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

        mMap.initViews();

        if (mAdapter == null)
        {
            mAdapter = new DownloadsViewPagerAdapter(mManager, 3);
        }

        mMap.registerAdapter(mAdapter);
    }

    @Override
    public Fragment getAdapterFragment(int position)
    {
        return mAdapter.getRegisteredFragment(position);
    }
}
