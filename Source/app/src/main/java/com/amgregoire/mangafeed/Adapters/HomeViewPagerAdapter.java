package com.amgregoire.mangafeed.Adapters;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.amgregoire.mangafeed.Fragments.CatalogFragment;
import com.amgregoire.mangafeed.Fragments.LibraryFragment;
import com.amgregoire.mangafeed.Fragments.RecentFragment;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomeViewPagerAdapter extends FragmentStatePagerAdapter
{

    private int mTabCount;
    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<Fragment>();

    public HomeViewPagerAdapter(FragmentManager aFragmentManager, int aTabCount)
    {
        super(aFragmentManager);

        this.mTabCount = aTabCount;
    }


    @Override
    public void restoreState(Parcelable state, ClassLoader loader)
    {
//        super.restoreState(state, loader);
    }

    @Override
    public Fragment getItem(int aPosition)
    {

        Fragment lFragment;
        switch (aPosition)
        {
            case 0:
                lFragment = RecentFragment.newInstance();
                mRegisteredFragments.put(0, lFragment);
                break;
            case 1:
                lFragment = LibraryFragment.newInstance();
                mRegisteredFragments.put(1, lFragment);
                break;
            default:
                lFragment = CatalogFragment.newInstance();
                mRegisteredFragments.put(2, lFragment);
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

