package com.amgregoire.mangafeed.v2.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun androidx.fragment.app.FragmentManager.lastFragment(): androidx.fragment.app.Fragment?
{
    val index = backStackEntryCount - 1

    if (index < 0) return null

    val backEntry = this.getBackStackEntryAt(index)
    return findFragmentByTag(backEntry.name)
}