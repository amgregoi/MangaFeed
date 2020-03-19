package com.amgregoire.mangafeed.v2.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import android.util.SparseArray
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.AllFragment
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.LibraryFragment
import com.amgregoire.mangafeed.v2.ui.catalog.fragment.RecentFragment
import java.lang.ref.WeakReference

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

class CatalogViewPagerAdapter(manager: androidx.fragment.app.FragmentManager, private val tabCount: Int = 3) : androidx.fragment.app.FragmentStatePagerAdapter(manager)
{
    private val references = SparseArray<WeakReference<androidx.fragment.app.Fragment>>()

    override fun getItem(position: Int): androidx.fragment.app.Fragment
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

