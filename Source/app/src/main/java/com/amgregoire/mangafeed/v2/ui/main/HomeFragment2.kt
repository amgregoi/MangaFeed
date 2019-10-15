package com.amgregoire.mangafeed.v2.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.TabLayout
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.AttrService
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.catalog.CatalogViewModel
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.content_bottom_filter.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment2 : BaseFragment()
{
    private val catalogViewModel by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProviders.of(parent).get(CatalogViewModel::class.java)
    }

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

        self.bottomSheetHeader.setOnClickListener {
            val bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(self.constraintLayoutBottomSheet)
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) setBottomSheetExpanded()
            else setBottomSheetCollapsed()
        }

        val lTextArea = self.searchView.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        lTextArea.setTextColor(resources.getColor(AttrService.getAttrColor(context!!, R.attr.text_color))) //or any color that you want
        self.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(p0: String?): Boolean = false
            override fun onQueryTextChange(query: String?): Boolean
            {
                query ?: return false
                catalogViewModel?.setQuery(query)
                return true
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean)
    {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) updateParentSettings()
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).hideToolbarElevation()
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_empty)
    }

    private fun setupViewPager()
    {
        self.viewPagerHome.adapter = CatalogViewPagerAdapter(childFragmentManager, 3)
        self.viewPagerHome.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(self.tabLayoutHome))
        self.viewPagerHome.offscreenPageLimit = 3
    }

    fun setBottomSheetCollapsed()
    {
        val bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(self.constraintLayoutBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        self.searchView.clearFocus()
    }

    fun setBottomSheetExpanded()
    {
        val bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(self.constraintLayoutBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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