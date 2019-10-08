package com.amgregoire.mangafeed.v2.ui

import android.support.v4.app.Fragment
import android.view.View

open abstract class BaseFragment : Fragment()
{
    lateinit var self: View

    open fun updateParentSettings()
    {
    }
}