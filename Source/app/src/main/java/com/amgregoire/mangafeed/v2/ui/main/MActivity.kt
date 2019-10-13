package com.amgregoire.mangafeed.v2.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentSettings
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.ui.AttrService
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.BaseNavigationActivity
import com.amgregoire.mangafeed.v2.ui.Logger
import com.amgregoire.mangafeed.v2.ui.catalog.CatalogViewModel
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*
import android.view.View.OnAttachStateChangeListener
import com.amgregoire.mangafeed.MangaFeed


class MActivity : BaseNavigationActivity()
{
    private val catalogViewModel by lazy {
        ViewModelProviders.of(this).get(CatalogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setupView()
    }

    fun setupView()
    {
        setContentView(R.layout.activity_m)

        supportFragmentManager.beginTransaction()
                .add(flContainer.id, MFragment.newInstance(), MFragment.TAG)
                .commit()

        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        return super.onCreateOptionsMenu(menu).also { setupSearchView(menu) }
    }

    private fun setupSearchView(menu: Menu)
    {
        menu.findItem(R.id.menuHomeSearch) ?: return

        val lSearch = menu.findItem(R.id.menuHomeSearch).actionView as SearchView
        val lTextArea = lSearch.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        lTextArea.setTextColor(resources.getColor(AttrService.getAttrColor(this, R.attr.text_color))) //or any color that you want
        lSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(s: String): Boolean
            {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean
            {
                catalogViewModel.setQuery(query)
                return true
            }
        })

        lSearch.addOnAttachStateChangeListener(object : OnAttachStateChangeListener
        {
            override fun onViewDetachedFromWindow(arg0: View)
            {
            }

            override fun onViewAttachedToWindow(arg0: View)
            {
                val query = catalogViewModel.queryFilter.value ?: ""
                lSearch.setQuery(query, true)
            }
        })
    }

    override fun setNavigationIcon(iconResource: Int?)
    {
        iconResource?.let {
            toolbar.setNavigationIcon(it)
        } ?: run { toolbar.navigationIcon = null }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        when (item?.itemId)
        {
            null -> Logger.debug("null menu item")
            R.id.menuAccountSettings -> replaceFragment(AccountFragmentSettings.newInstance(), AccountFragmentSettings.TAG)
            android.R.id.home -> onBackPressed()
        }

        return false
    }

    override fun onBackPressed()
    {
        val manager = supportFragmentManager

        if (manager.backStackEntryCount > 0)
        {
            val count = manager.backStackEntryCount
            if (manager.backStackEntryCount == 1)
            {
                setNavigationIcon(0)
                setOptionsMenu(R.menu.menu_toolbar_home)
                hideToolbarElevation()
            }
            manager.primaryNavigationFragment?.let { fragment ->

                if (fragment is BaseFragment)
                {
                    if (vMenuCover.visibility == View.VISIBLE) vMenuCover.visibility = View.GONE
                    fragment.onChildBackPress()
                    return
                }
            }
        }

        super.onBackPressed()
    }

    override fun getTheme(): Resources.Theme
    {
        val theme = super.getTheme()

        if (SharedPrefs.getLayoutTheme()) theme.applyStyle(R.style.AppTheme_Light, true)
        else theme.applyStyle(R.style.AppTheme_Dark, true)

        return theme
    }
}