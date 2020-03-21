package com.amgregoire.mangafeed.v2.ui.base

import android.view.View
import androidx.fragment.app.Fragment
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open abstract class BaseFragment : Fragment()
{
    lateinit var self: View

    fun user() = MangaFeed.app.user

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