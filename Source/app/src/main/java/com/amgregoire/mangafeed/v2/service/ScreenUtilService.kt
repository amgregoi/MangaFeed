package com.amgregoire.mangafeed.v2.service

import android.app.Activity
import android.util.DisplayMetrics


object ScreenUtilService {
    fun getScreenHeightPx(activity: Activity?): Int {
        activity ?: return 0

        val displayMetrics = DisplayMetrics()
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}