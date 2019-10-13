package com.amgregoire.mangafeed.v2.ui.main

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment2 : BaseFragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_home, null)
        return self
    }

    override fun onStart()
    {
        super.onStart()
        updateParentSettings()
        setupViewPager()
        setupTabLayout()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean)
    {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser) updateParentSettings()
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).hideToolbarElevation()
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_toolbar_home)
    }

    private fun setupViewPager()
    {
        self.viewPagerHome.adapter = CatalogViewPagerAdapter(childFragmentManager, 3)
        self.viewPagerHome.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(self.tabLayoutHome))
        self.viewPagerHome.offscreenPageLimit = 3
    }

    /***
     * This function sets up the tab layout.
     *
     */
    private fun setupTabLayout()
    {
        val tabLayout = self.tabLayoutHome
        tabLayout.addTab(tabLayout.newTab().setText("Recent"))
        tabLayout.addTab(tabLayout.newTab().setText("Library"))
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab)
            {
                self.viewPagerHome.setCurrentItem(tab.position, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab)
            {
                // do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab)
            {
                // do nothing
            }
        })
    }

    companion object
    {
        val TAG: String = HomeFragment2::class.java.simpleName

        fun newInstance() = HomeFragment2()
    }
}