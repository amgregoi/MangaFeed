package com.amgregoire.mangafeed.v2

import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs

enum class NavigationType
{
    Back, Close, Down
}

object ResourceFactory
{
    fun getNavigationIcon(navigationType: NavigationType): Int
    {
        val isDarkTheme = SharedPrefs.isLightTheme()
        when (navigationType)
        {
            NavigationType.Back ->
            {
                return if (isDarkTheme) R.drawable.nav_back_black
                else R.drawable.nav_back_white
            }
            NavigationType.Close ->
            {
                return if (isDarkTheme) R.drawable.nav_close_black
                else R.drawable.nav_close_white
            }
            NavigationType.Down ->
            {
                return if (isDarkTheme) R.drawable.nav_down_black
                else R.drawable.nav_down_white
            }
        }
    }
}