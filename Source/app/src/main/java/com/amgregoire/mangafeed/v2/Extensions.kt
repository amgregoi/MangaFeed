package com.amgregoire.mangafeed.v2

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

fun FragmentManager.lastFragment(): Fragment?
{
    val index = backStackEntryCount - 1

    if (index < 0) return null

    val backEntry = this.getBackStackEntryAt(index)
    return findFragmentByTag(backEntry.name)
}