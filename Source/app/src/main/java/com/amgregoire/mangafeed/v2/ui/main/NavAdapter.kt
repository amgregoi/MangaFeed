package com.amgregoire.mangafeed.v2.ui.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.amgregoire.mangafeed.UI.Fragments.AccountFragment
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragment


class NavAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager)
{
    override fun getItem(position: Int): Fragment
    {
        return when (position)
        {
            0 -> HomeFragment2.newInstance()
            1 -> DownloadsFragment.newInstance()
            else -> AccountFragment.newInstance()
        }
    }

    override fun getCount(): Int
    {
        return 3
    }

}