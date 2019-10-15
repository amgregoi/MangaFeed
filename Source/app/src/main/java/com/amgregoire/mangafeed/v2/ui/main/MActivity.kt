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
            if (manager.backStackEntryCount == 1)
            {
                setNavigationIcon(0)
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