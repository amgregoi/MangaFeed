package com.amgregoire.mangafeed.v2.ui.main

//import com.amgregoire.mangafeed.UI.Fragments.AccountFragment
import android.util.SparseArray
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.download.DownloadFragment
import com.amgregoire.mangafeed.v2.ui.settings.AccountFragment
import java.lang.ref.WeakReference


class NavAdapter(manager: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(manager)
{
    private val references = SparseArray<WeakReference<androidx.fragment.app.Fragment>>()

    override fun getItem(position: Int): androidx.fragment.app.Fragment
    {
        val fragment = when (position)
        {
            0 -> HomeFragment2.newInstance()
            1 -> DownloadFragment.newInstance()
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
        return references.get(position)?.get() as? BaseFragment
    }
}