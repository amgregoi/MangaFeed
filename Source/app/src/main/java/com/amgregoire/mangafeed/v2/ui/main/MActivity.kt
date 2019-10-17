package com.amgregoire.mangafeed.v2.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentSettings
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.CloudflareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.BaseNavigationActivity
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*
import kotlinx.coroutines.launch


class MActivity : BaseNavigationActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val source = MangaFeed.app.currentSource
        if (source.requiresCloudFlare())
        {
            CloudflareService().getCookies(source.baseUrl, NetworkService.defaultUserAgent) {
                uiScope.launch { setupView() }
            }
        }
        else setupView()

    }

    private fun setupView()
    {
        setContentView(R.layout.activity_m)
        setSupportActionBar(toolbar)

        val fragment = MFragment.newInstance()
        fragment.enterTransition = Fade(Fade.MODE_IN)
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(fragment)
                .add(flContainer.id, fragment, MFragment.TAG)
                .commit()

        setupBackStackListener()
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

    private fun setupBackStackListener()
    {
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.primaryNavigationFragment ?: return@addOnBackStackChangedListener
            (fragment as BaseFragment).updateParentSettings()
        }
    }

    override fun getTheme(): Resources.Theme
    {
        val theme = super.getTheme()

        if (SharedPrefs.isLightTheme()) theme.applyStyle(R.style.AppTheme_Light, true)
        else theme.applyStyle(R.style.AppTheme_Dark, true)

        return theme
    }
}