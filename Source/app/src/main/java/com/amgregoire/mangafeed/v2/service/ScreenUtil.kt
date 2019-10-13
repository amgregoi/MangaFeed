package com.amgregoire.mangafeed.v2.service

import android.R
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager


/**
 * Created by amgregoi on 12/4/18.
 */

object ScreenUtil
{
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Float
    {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float
    {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getScreenWidth(context: Context): Int
    {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    fun dpToPx(context: Context, value: Int): Int
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics)
                .toInt()
    }

    fun getScreenHeight(context: Context): Int
    {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

    fun getStatusBarHeight(resources: Resources): Int
    {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
        {
            result = resources.getDimensionPixelSize(resourceId)
        }

        return result
    }

    fun getNavigationBarHeight(resources: Resources): Int
    {
        var lResult = 0
        val lResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (lResourceId > 0)
        {
            lResult = resources.getDimensionPixelSize(lResourceId)
        }

        return lResult
    }

    fun getToolbarHeight(context: Context): Int
    {
        val styleAttr = context.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
        val toolbarHeight = styleAttr.getDimension(0, 0f).toInt()
        styleAttr.recycle()
        return toolbarHeight
    }
}
