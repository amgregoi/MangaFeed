package com.amgregoire.mangafeed.v2.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_m.view.*


class MFragment : BaseFragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_m, null)
        return self
    }

    override fun onResume()
    {
        super.onResume()
        updateParentSettings()
        Logger.error("$TAG - ON RESUME")
    }

    override fun updateParentSettings()
    {
        (self.vpNavigation.adapter as? NavAdapter)?.getFragment(self.vpNavigation.currentItem)?.updateParentSettings()
    }

    override fun onStart()
    {
        super.onStart()

        if (self.vpNavigation.adapter != null) return
        Logger.error("$TAG - (RE)setting adapter")

        self.vpNavigation.adapter = NavAdapter(childFragmentManager)
        self.vpNavigation.offscreenPageLimit = 2
        self.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId)
            {
                R.id.menuBottomNavCatalog -> self.vpNavigation.setCurrentItem(0, false)
                R.id.menuBottomNavDownloads -> self.vpNavigation.setCurrentItem(1, false)
                R.id.menuBottomNavAccount -> self.vpNavigation.setCurrentItem(2, false)
            }
            true
        }
    }

    /**********************************************************************************
     *
     *
     *
     */

    companion object
    {
        val TAG: String = "MFragment"
        fun newInstance() = MFragment()
    }
}