package com.amgregoire.mangafeed.v2.ui.main

import android.os.Bundle
import com.amgregoire.mangafeed.R
import kotlinx.android.synthetic.main.widget_toolbar_2.*
import android.content.res.Resources
import com.amgregoire.mangafeed.v2.ui._NavigationActivity


class MActivity : _NavigationActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_m)

        addFragment(MFragment(), "tag")
    }

    override fun setNavigationIcon(iconResource: Int?)
    {
        iconResource?.let {
            toolbar.setNavigationIcon(it)
        } ?: run { toolbar.navigationIcon = null }
    }

    override fun getTheme(): Resources.Theme
    {
        val theme = super.getTheme()

        if (false)  theme.applyStyle(R.style.AppTheme_Dark, true)
        else theme.applyStyle(R.style.AppTheme_Light, true)

        return theme
    }
}