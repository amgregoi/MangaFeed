package com.amgregoire.mangafeed.v2.ui.catalog

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.amgregoire.mangafeed.UI.Fragments.HomeFragmentCatalog
import com.amgregoire.mangafeed.UI.Fragments.HomeFragmentLibrary
import com.amgregoire.mangafeed.UI.Fragments.HomeFragmentRecent

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

class HomeViewPagerAdapter2(manager: FragmentManager, private val tabCount: Int = 3) : FragmentStatePagerAdapter(manager)
{
    override fun getItem(aPosition: Int): Fragment
    {
        return when (aPosition)
        {
            0 -> RecentFragment.newInstance()
            1 -> LibraryFragment.newInstance()
            else -> HomeFragmentCatalog.newInstance()
        }
    }

    override fun getCount(): Int
    {
        return tabCount
    }
}

