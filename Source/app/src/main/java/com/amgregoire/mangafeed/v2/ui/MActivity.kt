package com.amgregoire.mangafeed.v2.ui

import android.os.Bundle
import android.view.View
import com.amgregoire.mangafeed.R
import kotlinx.android.synthetic.main.widget_toolbar_2.*

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

    override fun setTitle(title: String)
    {
        toolbarTitle.visibility = View.VISIBLE
        toolbarTitle.text = title
    }
}