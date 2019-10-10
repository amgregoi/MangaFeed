package com.amgregoire.mangafeed.v2.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui._NavigationActivity
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*


class MActivity : _NavigationActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
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
            null ->
            {
            }
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

        if (false) theme.applyStyle(R.style.AppTheme_Dark, true)
        else theme.applyStyle(R.style.AppTheme_Light, true)

        return theme
    }
}