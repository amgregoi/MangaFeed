package com.amgregoire.mangafeed.v2.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.content.res.Resources
import android.os.Bundle
import android.transition.Fade
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentSettings
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.lastFragment
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.BaseFragment
import com.amgregoire.mangafeed.v2.ui.BaseNavigationActivity
import com.amgregoire.mangafeed.v2.ui.catalog.CatalogViewModel
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*
import kotlinx.coroutines.launch


class MActivity : BaseNavigationActivity()
{
    val catalogViewModel by lazy {
        ViewModelProviders.of(this).get(CatalogViewModel::class.java)
    }

    val mainFragment: MFragment by lazy { MFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val source = MangaFeed.app.currentSource
        if (source.requiresCloudFlare())
        {
            CloudFlareService().getCookies(source.baseUrl, NetworkService.defaultUserAgent) {
                uiScope.launch { setupView() }
            }
        }
        else setupView()

    }

    private fun setupView()
    {
        setContentView(R.layout.activity_m)
        setSupportActionBar(toolbar)

        setupToolbarSpinner()

        mainFragment.enterTransition = Fade(Fade.MODE_IN)
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(mainFragment)
                .add(flContainer.id, mainFragment, MFragment.TAG)
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
                showSpinner()
                hideToolbarElevation()
            }

            manager.lastFragment()?.let { fragment ->

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
            val fragment = supportFragmentManager.lastFragment() ?: return@addOnBackStackChangedListener
            (fragment as BaseFragment).updateParentSettings()
        }
    }

    private fun setupToolbarSpinner()
    {
        toolbarSpinner.adapter = ArrayAdapter(this, R.layout.item_source_spinner, MangaEnums.Source.values())
        toolbarSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                val newSource = MangaEnums.Source.values()[position]
                SharedPrefs.setSavedSource(newSource.name)
                catalogViewModel.setSource(newSource.source)
                Logger.error("${newSource} selected")
            }
        }

        val position = MangaEnums.Source.getPosition(MangaFeed.app.currentSource.sourceName)
        toolbarSpinner.setSelection(position)
    }

    override fun getTheme(): Resources.Theme
    {
        val theme = super.getTheme()

        if (SharedPrefs.isLightTheme()) theme.applyStyle(R.style.AppTheme_Light, true)
        else theme.applyStyle(R.style.AppTheme_Dark, true)

        return theme
    }
}