package com.amgregoire.mangafeed.UI.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragmentDownloading;
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragmentSaved;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class DownloadsViewPagerAdapter extends FragmentStatePagerAdapter
{

    private int mTabCount;
    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<Fragment>();

    public DownloadsViewPagerAdapter(FragmentManager aFragmentManager, int aTabCount)
    {
        super(aFragmentManager);

        this.mTabCount = aTabCount;
    }

    @Override
    public Fragment getItem(int aPosition)
    {

        Fragment lFragment;
        switch (aPosition)
        {
            case 0:
                lFragment = DownloadsFragmentSaved.newInstance();
                mRegisteredFragments.put(0, lFragment);
                break;
            default:
                lFragment = DownloadsFragmentDownloading.newInstance();
                mRegisteredFragments.put(1, lFragment);
        }

        return lFragment;
    }

    @Override
    public int getCount()
    {
        return mTabCount;
    }

    /***
     * This function returns the specified fragment, or a new instance of that fragment if it's been GC'd
     * @param aPosition
     * @return
     */
    public Fragment getRegisteredFragment(int aPosition)
    {
        if (mRegisteredFragments == null)
        {
            mRegisteredFragments = new SparseArray<>();
        }

        Fragment lResult = mRegisteredFragments.get(aPosition);
        return (lResult == null) ? getItem(aPosition) : lResult;
    }
}

