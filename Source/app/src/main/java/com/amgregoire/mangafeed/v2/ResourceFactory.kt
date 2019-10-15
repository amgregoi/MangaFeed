package com.amgregoire.mangafeed.v2

import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs

object ResourceFactory
{
    fun getNavigationIcon():Int
    {
        return if(SharedPrefs.getLayoutTheme()) R.drawable.navigation_back_black
        else R.drawable.navigation_back
    }
}