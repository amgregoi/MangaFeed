package com.amgregoire.mangafeed.v2.ui.catalog.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.amgregoire.mangafeed.v2.ui.catalog.AllFragment
import com.amgregoire.mangafeed.v2.ui.catalog.LibraryFragment
import com.amgregoire.mangafeed.v2.ui.catalog.RecentFragment

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
            else -> AllFragment.newInstance()
        }
    }

    override fun getCount(): Int
    {
        return tabCount
    }
}

