package com.amgregoire.mangafeed.v2.ui

import android.support.v4.app.Fragment
import android.view.View

open abstract class BaseFragment : Fragment()
{
    lateinit var self: View

    open fun onChildBackPress()
    {
        if(childFragmentManager.backStackEntryCount > 0) childFragmentManager.popBackStack()
        else
        {
            val parent = activity?: return
            (parent as FragmentNavMap).popBackStack()
        }
    }

    open fun updateParentSettings()
    {
    }
}