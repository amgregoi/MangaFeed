package com.amgregoire.mangafeed.v2.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.transition.Fade
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.NavigationType
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.extension.lastFragment
import com.amgregoire.mangafeed.v2.service.AttrService
import com.amgregoire.mangafeed.v2.service.KeyboardUtil
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.base.BaseNavigationActivity
import com.amgregoire.mangafeed.v2.ui.filter.FilterFragment
import com.amgregoire.mangafeed.v2.ui.filter.FilterVM
import com.amgregoire.mangafeed.v2.ui.main.MFragment
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.widget_toolbar_2.toolbar
import kotlinx.android.synthetic.main.widget_toolbar_filter.*


class FilterActivity : BaseNavigationActivity()
{
    private val filterVM by lazy { ViewModelProviders.of(this).get(FilterVM::class.java) }
    private val filterFragment: FilterFragment by lazy { FilterFragment.newInstance() }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setupView()
    }

    private fun setupView()
    {
        setContentView(R.layout.activity_filter)
        setSupportActionBar(toolbar)

        filterFragment.enterTransition = Fade(Fade.MODE_IN)
        supportFragmentManager.beginTransaction()
                .setPrimaryNavigationFragment(filterFragment)
                .add(flContainer.id, filterFragment, MFragment.TAG)
                .commit()

        setupBackStackListener()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean
            {
                filterVM.updateQuery(newText)
                return true
            }
        })

        val txtSearch = searchView.findViewById(R.id.search_src_text) as EditText
        val color = getColor(AttrService.getAttrColor(this, R.attr.text_color))
        txtSearch.setHintTextColor(color)
        txtSearch.setTextColor(color)

        searchView.requestFocus()
        KeyboardUtil.show(this)
        setNavigationIcon(ResourceFactory.getNavigationIcon(NavigationType.Close))
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
                searchView.requestFocus()
                KeyboardUtil.show(this)
                filterFragment.updateParentSettings()
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

    override fun getTheme(): Resources.Theme
    {
        val theme = super.getTheme()

        if (SharedPrefs.isLightTheme()) theme.applyStyle(R.style.AppTheme_Light, true)
        else theme.applyStyle(R.style.AppTheme_Dark, true)

        return theme
    }

    companion object
    {
        fun newInstance(context: Context) = Intent(context, FilterActivity::class.java)
    }
}