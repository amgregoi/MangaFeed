package com.amgregoire.mangafeed.v2.ui.main

//import com.amgregoire.mangafeed.UI.Fragments.AccountFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.SparseArray
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragment
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.settings.AccountFragment
import java.lang.ref.WeakReference


class NavAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager)
{
    private val references = SparseArray<WeakReference<Fragment>>()

    override fun getItem(position: Int): Fragment
    {
        val fragment = when (position)
        {
            0 -> HomeFragment2.newInstance()
            1 -> DownloadsFragment.newInstance()
            else -> AccountFragment.newInstance()
        }

        references.put(position, WeakReference(fragment))
        return fragment
    }

    override fun getCount(): Int
    {
        return 3
    }

    fun getFragment(position: Int): BaseFragment?
    {
        return references.get(position).get() as? BaseFragment
    }
}