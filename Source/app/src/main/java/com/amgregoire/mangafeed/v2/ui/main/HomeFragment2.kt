package com.amgregoire.mangafeed.v2.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.catalog.vm.CatalogVM
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
        Logger.error("$TAG - ON START")

        updateParentSettings()
        setupViewPager()
        setupTabLayout()


//        val lTextArea = self.searchView.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
//        lTextArea.setTextColor(resources.getColor(AttrService.getAttrColor(context!!, R.attr.text_color))) //or any color that you want
//        self.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
//        {
//            override fun onQueryTextSubmit(p0: String?): Boolean = false
//            override fun onQueryTextChange(query: String?): Boolean
//            {
//                query ?: return false
//                catalogViewModel?.setQuery(query)
//                return true
//            }
//        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean)
    {
        if (isVisibleToUser) updateParentSettings()
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).showSpinner()
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_toolbar_filter)
        (self.viewPagerHome.adapter as? CatalogViewPagerAdapter)?.updateItems()
    }

    private fun setupViewPager()
    {
        if(self.viewPagerHome.adapter != null) return

        Logger.error("$TAG - Setting adapter thing")
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

        if(tabLayout.tabCount > 0) return

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