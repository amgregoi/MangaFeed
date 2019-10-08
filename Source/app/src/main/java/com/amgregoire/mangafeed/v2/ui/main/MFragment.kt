package com.amgregoire.mangafeed.v2.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.main.NavAdapter
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
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
    }

    override fun onStart()
    {
        super.onStart()
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

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).setTitle(SharedPrefs.getSavedSource())
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_toolbar_home)
    }
}