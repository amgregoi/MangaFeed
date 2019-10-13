package com.amgregoire.mangafeed.v2.ui

import android.support.v4.app.Fragment
import android.view.View
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open abstract class BaseFragment : Fragment()
{
    lateinit var self: View

    open fun onChildBackPress()
    {
        if (childFragmentManager.backStackEntryCount > 0) childFragmentManager.popBackStack()
        else
        {
            val parent = activity ?: return
            (parent as FragmentNavMap).popBackStack()
        }
    }

    protected fun delayedAction(delayMs: Long = 250, action: () -> Unit) = ioScope.launch {
        delay(delayMs)
        uiScope.launch { action.invoke() }
    }

    open fun updateParentSettings()
    {
    }
}