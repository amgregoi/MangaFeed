package com.amgregoire.mangafeed.v2.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.transition.Fade
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.extension.lastFragment
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.BaseNavigationActivity
import com.amgregoire.mangafeed.v2.ui.catalog.vm.CatalogVM
import com.amgregoire.mangafeed.v2.ui.main.MFragment
import kotlinx.android.synthetic.main.activity_m.*
import kotlinx.android.synthetic.main.widget_toolbar_2.*
import kotlinx.coroutines.launch


class MActivity : BaseNavigationActivity()
{
    private val sourceVM by lazy { ViewModelProviders.of(this).get(CatalogVM::class.java) }
    private val mainFragment: MFragment by lazy { MFragment.newInstance() }

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
            android.R.id.home -> onBackPressed()
            R.id.menuHomeSearch -> Logger.debug("Need to implement search + filter fragment")
            else -> Logger.debug("Unknown menu item selected -> $item -> ${item.menuInfo}")
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
                setNavigationIcon(null)
                mainFragment.updateParentSettings()
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
                sourceVM.setSource(newSource.source)
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

    companion object
    {
        fun newInstance(context: Context) = Intent(context, MActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
    }
}