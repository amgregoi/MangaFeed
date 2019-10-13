package com.amgregoire.mangafeed.v2.ui.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.SparseArray
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.AllFragment
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.LibraryFragment
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.RecentFragment
import java.lang.ref.WeakReference

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

class CatalogViewPagerAdapter(manager: FragmentManager, private val tabCount: Int = 3) : FragmentStatePagerAdapter(manager)
{
    private val references = SparseArray<WeakReference<Fragment>>()

    override fun getItem(position: Int): Fragment
    {
        return references.get(position)?.get() ?: with(position) {
            val fragment = when (position)
            {
                0 -> RecentFragment.newInstance()
                1 -> LibraryFragment.newInstance()
                else -> AllFragment.newInstance()
            }

            references.put(position, WeakReference(fragment))

            return fragment
        }
    }


    override fun getCount(): Int
    {
        return tabCount
    }
}

